import * as admin from 'firebase-admin';
import { onDocumentUpdated } from 'firebase-functions/v2/firestore';
import { logger } from 'firebase-functions';
import { onCall, HttpsError } from 'firebase-functions/v2/https';

admin.initializeApp();

const db = admin.firestore();

// OJO: estos nombres deben coincidir con tus colecciones reales.
const GAMES_COLLECTION = 'games';
const PREDICTIONS_COLLECTION = 'gamesUsers';
const USERS_COLLECTION = 'users';
const TEAMS_COLLECTION = 'teams';
const STANDINGS_COLLECTION = 'standings';

type ResultSign = 'HOME_WIN' | 'DRAW' | 'AWAY_WIN';

function toResultSign(homeGoals: number, awayGoals: number): ResultSign {
  if (homeGoals > awayGoals) return 'HOME_WIN';
  if (homeGoals < awayGoals) return 'AWAY_WIN';
  return 'DRAW';
}

/**
 * Algoritmo de puntos:
 * - 5 puntos si acierta el marcador exacto.
 * - 3 puntos si acierta solo el signo.
 * - -2 si invierte el marcador exacto.
 * - -1 para cualquier otro error.
 */
function computePoints(
  actualHome: number,
  actualAway: number,
  predictedHome: number,
  predictedAway: number
  ): number {
  const actualSign = toResultSign(actualHome, actualAway);
  const predictedSign = toResultSign(predictedHome, predictedAway);

  if (predictedHome === actualHome && predictedAway === actualAway) {
    return 5;
  }

  if (predictedSign === actualSign) {
    return 3;
  }

  if (predictedHome === actualAway && predictedAway === actualHome) {
    return -2;
  }

  return -1;
}

// ------------------------------------------------------
// Estructuras para llaves de eliminación (KO)
// ------------------------------------------------------

export type MatchStage =
| 'GROUP'
| 'ROUND_OF_32'
| 'ROUND_OF_16'
| 'QUARTER_FINAL'
| 'SEMI_FINAL'
| 'THIRD_PLACE'
| 'FINAL';

export type SeedDescriptor =
| { type: 'GROUP_POSITION'; group: string; position: number }
| { type: 'BEST_THIRD'; rank: number }
| { type: 'WINNER_OF_MATCH'; stage: MatchStage; matchNumber: number }
| { type: 'LOSER_OF_MATCH'; stage: MatchStage; matchNumber: number };

export interface KnockoutSlot {
  stage: MatchStage;
  matchNumber: number;
  homeSource: SeedDescriptor;
  awaySource: SeedDescriptor;
}

// ------------------------------------------------------
// Cálculo de standings de grupos + mejores terceros
// ------------------------------------------------------

export interface TeamDoc {
  id: string;
  name: string;
  group: string;
  flagCode?: string;
}

export interface GameDoc {
  tournamentId: string;
  homeTeamId: string;
  awayTeamId: string;
  stage: MatchStage;
  group?: string;
  homeGoals?: number | null;
  awayGoals?: number | null;
  status?: string;
}

export interface GroupStanding {
  teamId: string;
  teamName: string;
  flagUrl: string;
  group: string;
  played: number;
  wins: number;
  draws: number;
  losses: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDiff: number;
  points: number;
  position: number;
}

interface MutableTeamStats {
  teamId: string;
  teamName: string;
  flagUrl: string;
  group: string;
  played: number;
  wins: number;
  draws: number;
  losses: number;
  goalsFor: number;
  goalsAgainst: number;
  points: number;
}

interface MiniStanding {
  teamId: string;
  points: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDiff: number;
}

function compareOverallStats(a: GroupStanding, b: GroupStanding): number {
  if (a.points !== b.points) return b.points - a.points;
  if (a.goalDiff !== b.goalDiff) return b.goalDiff - a.goalDiff;
  if (a.goalsFor !== b.goalsFor) return b.goalsFor - a.goalsFor;
  return 0;
}

function compareMiniTableStats(a: MiniStanding, b: MiniStanding): number {
  if (a.points !== b.points) return b.points - a.points;
  if (a.goalDiff !== b.goalDiff) return b.goalDiff - a.goalDiff;
  if (a.goalsFor !== b.goalsFor) return b.goalsFor - a.goalsFor;
  return 0;
}

function buildMiniTableForTiedTeams(
  tiedRows: GroupStanding[],
  groupGames: GameDoc[]
  ): Record<string, MiniStanding> {
  const tiedTeamIds = new Set(tiedRows.map((r) => r.teamId));

  const miniTable: Record<string, MiniStanding> = {};

  for (const row of tiedRows) {
    miniTable[row.teamId] = {
      teamId: row.teamId,
      points: 0,
      goalsFor: 0,
      goalsAgainst: 0,
      goalDiff: 0,
    };
  }

  for (const game of groupGames) {
    if (game.status !== 'FINISHED') continue;
    if (typeof game.homeGoals !== 'number' || typeof game.awayGoals !== 'number') continue;
    if (!tiedTeamIds.has(game.homeTeamId) || !tiedTeamIds.has(game.awayTeamId)) continue;

    const home = miniTable[game.homeTeamId];
    const away = miniTable[game.awayTeamId];

    home.goalsFor += game.homeGoals;
    home.goalsAgainst += game.awayGoals;

    away.goalsFor += game.awayGoals;
    away.goalsAgainst += game.homeGoals;

    if (game.homeGoals > game.awayGoals) {
      home.points += 3;
    } else if (game.homeGoals < game.awayGoals) {
      away.points += 3;
    } else {
      home.points += 1;
      away.points += 1;
    }
  }

  for (const teamId of Object.keys(miniTable)) {
    const row = miniTable[teamId];
    row.goalDiff = row.goalsFor - row.goalsAgainst;
  }

  return miniTable;
}

function hasSameMiniStats(a: MiniStanding, b: MiniStanding): boolean {
  return (
    a.points === b.points &&
    a.goalDiff === b.goalDiff &&
    a.goalsFor === b.goalsFor
    );
}

function groupTiedMiniRows(rows: GroupStanding[], miniTable: Record<string, MiniStanding>): GroupStanding[][] {
  const groups: GroupStanding[][] = [];

  for (const row of rows) {
    const lastGroup = groups[groups.length - 1];
    if (!lastGroup || lastGroup.length === 0) {
      groups.push([row]);
      continue;
    }

    const lastRow = lastGroup[lastGroup.length - 1];
    const currentMini = miniTable[row.teamId];
    const lastMini = miniTable[lastRow.teamId];

    if (hasSameMiniStats(currentMini, lastMini)) {
      lastGroup.push(row);
    } else {
      groups.push([row]);
    }
  }

  return groups;
}

function resolveTiedSubset(
  tiedRows: GroupStanding[],
  groupGames: GameDoc[]
): GroupStanding[] {
  if (tiedRows.length <= 1) return tiedRows;

  const miniTable = buildMiniTableForTiedTeams(tiedRows, groupGames);

  const rowsSortedByMini = [...tiedRows].sort((a, b) => {
    return compareMiniTableStats(miniTable[a.teamId], miniTable[b.teamId]);
  });

  const tiedMiniGroups = groupTiedMiniRows(rowsSortedByMini, miniTable);

  const resolved: GroupStanding[] = [];

  for (const group of tiedMiniGroups) {
    if (group.length === 1) {
      resolved.push(group[0]);
      continue;
    }

    // Si el subgrupo es menor que el grupo original,
    // significa que algunos ya se separaron y hay que reaplicar
    // el head-to-head solo entre los que siguen empatados.
    if (group.length < tiedRows.length) {
      resolved.push(...resolveTiedSubset(group, groupGames));
      continue;
    }

    // Si todo el grupo original sigue exactamente empatado
    // tras aplicar head-to-head, pasamos al fallback global.
    const fallbackSorted = [...group].sort((a, b) => {
      if (a.goalDiff !== b.goalDiff) return b.goalDiff - a.goalDiff;
      if (a.goalsFor !== b.goalsFor) return b.goalsFor - a.goalsFor;
      return 0;
    });

    resolved.push(...fallbackSorted);
  }

  return resolved;
}

function resolveGroupOrder(
  rows: GroupStanding[],
  groupGames: GameDoc[]
  ): GroupStanding[] {
  if (rows.length <= 1) return rows;

  const rowsByOverall = [...rows].sort(compareOverallStats);

  const resolved: GroupStanding[] = [];
  let i = 0;

  while (i < rowsByOverall.length) {
    const current = rowsByOverall[i];
    const tiedGroup = [current];
    let j = i + 1;

    while (j < rowsByOverall.length) {
      const next = rowsByOverall[j];
      if (next.points === current.points) {
        tiedGroup.push(next);
        j++;
      } else {
        break;
      }
    }

    if (tiedGroup.length === 1) {
      resolved.push(current);
    } else {
      resolved.push(...resolveTiedSubset(tiedGroup, groupGames));
    }

    i = j;
  }

  return resolved;
}

export function computeStandingsAndBestThirds(
  teams: TeamDoc[],
  games: GameDoc[]
  ): {
  standingsByGroup: Record<string, GroupStanding[]>;
  bestThirdGlobal: GroupStanding[];
} {
  const statsByGroup: Record<string, Record<string, MutableTeamStats>> = {};
  const teamsById: Record<string, TeamDoc> = {};

  teams.forEach((t) => {
    teamsById[t.id] = t;
  });

  for (const team of teams) {
    const group = team.group;
    if (!statsByGroup[group]) {
      statsByGroup[group] = {};
    }
    statsByGroup[group][team.id] = {
      teamId: team.id,
      teamName: team.name,
      flagUrl: team.flagCode || '',
      group,
      played: 0,
      wins: 0,
      draws: 0,
      losses: 0,
      goalsFor: 0,
      goalsAgainst: 0,
      points: 0,
    };
  }

  for (const game of games) {
    if (game.stage !== 'GROUP') continue;
    if (!game.group) continue;

    const { homeGoals, awayGoals, status } = game;

    if (
      status !== 'FINISHED' ||
      typeof homeGoals !== 'number' ||
      typeof awayGoals !== 'number'
      ) {
      continue;
  }

  const group = game.group;
  if (!statsByGroup[group]) {
    statsByGroup[group] = {};
  }

  const homeTeamInfo = teamsById[game.homeTeamId];
  const awayTeamInfo = teamsById[game.awayTeamId];

  if (!statsByGroup[group][game.homeTeamId]) {
    statsByGroup[group][game.homeTeamId] = {
      teamId: game.homeTeamId,
      teamName: homeTeamInfo?.name || game.homeTeamId,
      flagUrl: homeTeamInfo?.flagCode || '',
      group,
      played: 0,
      wins: 0,
      draws: 0,
      losses: 0,
      goalsFor: 0,
      goalsAgainst: 0,
      points: 0,
    };
  }

  if (!statsByGroup[group][game.awayTeamId]) {
    statsByGroup[group][game.awayTeamId] = {
      teamId: game.awayTeamId,
      teamName: awayTeamInfo?.name || game.awayTeamId,
      flagUrl: awayTeamInfo?.flagCode || '',
      group,
      played: 0,
      wins: 0,
      draws: 0,
      losses: 0,
      goalsFor: 0,
      goalsAgainst: 0,
      points: 0,
    };
  }

  const homeStats = statsByGroup[group][game.homeTeamId];
  const awayStats = statsByGroup[group][game.awayTeamId];

  homeStats.played++;
  awayStats.played++;

  homeStats.goalsFor += homeGoals;
  homeStats.goalsAgainst += awayGoals;

  awayStats.goalsFor += awayGoals;
  awayStats.goalsAgainst += homeGoals;

  if (homeGoals > awayGoals) {
    homeStats.wins++;
    homeStats.points += 3;
    awayStats.losses++;
  } else if (homeGoals < awayGoals) {
    awayStats.wins++;
    awayStats.points += 3;
    homeStats.losses++;
  } else {
    homeStats.draws++;
    awayStats.draws++;
    homeStats.points += 1;
    awayStats.points += 1;
  }
}

const standingsByGroup: Record<string, GroupStanding[]> = {};

for (const group of Object.keys(statsByGroup)) {
  const statsMap = statsByGroup[group];
  const rows: GroupStanding[] = Object.values(statsMap).map((s) => ({
    teamId: s.teamId,
    teamName: s.teamName,
    flagUrl: s.flagUrl,
    group: s.group,
    played: s.played,
    wins: s.wins,
    draws: s.draws,
    losses: s.losses,
    goalsFor: s.goalsFor,
    goalsAgainst: s.goalsAgainst,
    goalDiff: s.goalsFor - s.goalsAgainst,
    points: s.points,
    position: 0,
  }));

  const groupGames = games.filter(
    (game) =>
    game.stage === 'GROUP' &&
    game.group === group &&
    game.status === 'FINISHED' &&
    typeof game.homeGoals === 'number' &&
    typeof game.awayGoals === 'number'
    );

  const orderedRows = resolveGroupOrder(rows, groupGames);

  orderedRows.forEach((row, idx) => {
    row.position = idx + 1;
  });

  standingsByGroup[group] = orderedRows;
}

const thirdCandidates: GroupStanding[] = [];

for (const group of Object.keys(standingsByGroup)) {
  const rows = standingsByGroup[group];
  const third = rows[2];
  if (third) thirdCandidates.push(third);
}

thirdCandidates.sort(compareOverallStats);

const bestThirdGlobal = thirdCandidates.slice(0, 8);

return { standingsByGroup, bestThirdGlobal };
}

// ------------------------------------------------------
// Plantillas knockout
// ------------------------------------------------------

export const ROUND_OF_32_SLOTS: KnockoutSlot[] = [
  { stage: 'ROUND_OF_32', matchNumber: 73, homeSource: { type: 'GROUP_POSITION', group: 'A', position: 2 }, awaySource: { type: 'GROUP_POSITION', group: 'B', position: 2 } },
  { stage: 'ROUND_OF_32', matchNumber: 74, homeSource: { type: 'GROUP_POSITION', group: 'E', position: 1 }, awaySource: { type: 'BEST_THIRD', rank: 1 } },
  { stage: 'ROUND_OF_32', matchNumber: 75, homeSource: { type: 'GROUP_POSITION', group: 'F', position: 1 }, awaySource: { type: 'GROUP_POSITION', group: 'C', position: 2 } },
  { stage: 'ROUND_OF_32', matchNumber: 76, homeSource: { type: 'GROUP_POSITION', group: 'C', position: 1 }, awaySource: { type: 'GROUP_POSITION', group: 'F', position: 2 } },
  { stage: 'ROUND_OF_32', matchNumber: 77, homeSource: { type: 'GROUP_POSITION', group: 'I', position: 1 }, awaySource: { type: 'BEST_THIRD', rank: 2 } },
  { stage: 'ROUND_OF_32', matchNumber: 78, homeSource: { type: 'GROUP_POSITION', group: 'E', position: 2 }, awaySource: { type: 'GROUP_POSITION', group: 'I', position: 2 } },
  { stage: 'ROUND_OF_32', matchNumber: 79, homeSource: { type: 'GROUP_POSITION', group: 'A', position: 1 }, awaySource: { type: 'BEST_THIRD', rank: 3 } },
  { stage: 'ROUND_OF_32', matchNumber: 80, homeSource: { type: 'GROUP_POSITION', group: 'L', position: 1 }, awaySource: { type: 'BEST_THIRD', rank: 4 } },
  { stage: 'ROUND_OF_32', matchNumber: 81, homeSource: { type: 'GROUP_POSITION', group: 'D', position: 1 }, awaySource: { type: 'BEST_THIRD', rank: 5 } },
  { stage: 'ROUND_OF_32', matchNumber: 82, homeSource: { type: 'GROUP_POSITION', group: 'G', position: 1 }, awaySource: { type: 'BEST_THIRD', rank: 6 } },
  { stage: 'ROUND_OF_32', matchNumber: 83, homeSource: { type: 'GROUP_POSITION', group: 'K', position: 2 }, awaySource: { type: 'GROUP_POSITION', group: 'L', position: 2 } },
  { stage: 'ROUND_OF_32', matchNumber: 84, homeSource: { type: 'GROUP_POSITION', group: 'H', position: 1 }, awaySource: { type: 'GROUP_POSITION', group: 'J', position: 2 } },
  { stage: 'ROUND_OF_32', matchNumber: 85, homeSource: { type: 'GROUP_POSITION', group: 'B', position: 1 }, awaySource: { type: 'BEST_THIRD', rank: 7 } },
  { stage: 'ROUND_OF_32', matchNumber: 86, homeSource: { type: 'GROUP_POSITION', group: 'J', position: 1 }, awaySource: { type: 'GROUP_POSITION', group: 'H', position: 2 } },
  { stage: 'ROUND_OF_32', matchNumber: 87, homeSource: { type: 'GROUP_POSITION', group: 'K', position: 1 }, awaySource: { type: 'BEST_THIRD', rank: 8 } },
  { stage: 'ROUND_OF_32', matchNumber: 88, homeSource: { type: 'GROUP_POSITION', group: 'D', position: 2 }, awaySource: { type: 'GROUP_POSITION', group: 'G', position: 2 } },
];

export const ROUND_OF_16_SLOTS: KnockoutSlot[] = [
  { stage: 'ROUND_OF_16', matchNumber: 89, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 73 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 74 } },
  { stage: 'ROUND_OF_16', matchNumber: 90, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 75 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 76 } },
  { stage: 'ROUND_OF_16', matchNumber: 91, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 77 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 78 } },
  { stage: 'ROUND_OF_16', matchNumber: 92, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 79 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 80 } },
  { stage: 'ROUND_OF_16', matchNumber: 93, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 81 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 82 } },
  { stage: 'ROUND_OF_16', matchNumber: 94, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 83 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 84 } },
  { stage: 'ROUND_OF_16', matchNumber: 95, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 85 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 86 } },
  { stage: 'ROUND_OF_16', matchNumber: 96, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 87 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_32', matchNumber: 88 } },
];

export const QUARTER_FINAL_SLOTS: KnockoutSlot[] = [
  { stage: 'QUARTER_FINAL', matchNumber: 97, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_16', matchNumber: 89 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_16', matchNumber: 90 } },
  { stage: 'QUARTER_FINAL', matchNumber: 98, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_16', matchNumber: 91 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_16', matchNumber: 92 } },
  { stage: 'QUARTER_FINAL', matchNumber: 99, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_16', matchNumber: 93 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_16', matchNumber: 94 } },
  { stage: 'QUARTER_FINAL', matchNumber: 100, homeSource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_16', matchNumber: 95 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'ROUND_OF_16', matchNumber: 96 } },
];

export const SEMI_FINAL_SLOTS: KnockoutSlot[] = [
  { stage: 'SEMI_FINAL', matchNumber: 101, homeSource: { type: 'WINNER_OF_MATCH', stage: 'QUARTER_FINAL', matchNumber: 97 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'QUARTER_FINAL', matchNumber: 98 } },
  { stage: 'SEMI_FINAL', matchNumber: 102, homeSource: { type: 'WINNER_OF_MATCH', stage: 'QUARTER_FINAL', matchNumber: 99 }, awaySource: { type: 'WINNER_OF_MATCH', stage: 'QUARTER_FINAL', matchNumber: 100 } },
];

export const THIRD_PLACE_SLOT: KnockoutSlot = {
  stage: 'THIRD_PLACE',
  matchNumber: 103,
  homeSource: { type: 'LOSER_OF_MATCH', stage: 'SEMI_FINAL', matchNumber: 101 },
  awaySource: { type: 'LOSER_OF_MATCH', stage: 'SEMI_FINAL', matchNumber: 102 },
};

export const FINAL_SLOT: KnockoutSlot = {
  stage: 'FINAL',
  matchNumber: 104,
  homeSource: { type: 'WINNER_OF_MATCH', stage: 'SEMI_FINAL', matchNumber: 101 },
  awaySource: { type: 'WINNER_OF_MATCH', stage: 'SEMI_FINAL', matchNumber: 102 },
};

const ALL_KNOCKOUT_SLOTS: KnockoutSlot[] = [
  ...ROUND_OF_32_SLOTS,
  ...ROUND_OF_16_SLOTS,
  ...QUARTER_FINAL_SLOTS,
  ...SEMI_FINAL_SLOTS,
  THIRD_PLACE_SLOT,
  FINAL_SLOT,
];

// ------------------------------------------------------
// Helpers knockout
// ------------------------------------------------------

interface FinishedKoGame {
  matchNumber: number;
  homeTeamId: string;
  awayTeamId: string;
  homeGoals: number;
  awayGoals: number;
  wentToPenalties?: boolean;
  homePenalties?: number | null;
  awayPenalties?: number | null;
}

function getWinner(g: FinishedKoGame): string {
  if (g.homeGoals > g.awayGoals) return g.homeTeamId;
  if (g.awayGoals > g.homeGoals) return g.awayTeamId;

  if (
    g.wentToPenalties &&
    typeof g.homePenalties === 'number' &&
    typeof g.awayPenalties === 'number'
    ) {
    return g.homePenalties > g.awayPenalties ? g.homeTeamId : g.awayTeamId;
}

return '';
}

function getLoser(g: FinishedKoGame): string {
  const winner = getWinner(g);
  if (!winner) return '';
  return winner === g.homeTeamId ? g.awayTeamId : g.homeTeamId;
}

function resolveTeamId(
  seed: SeedDescriptor,
  standingsByGroup: Record<string, GroupStanding[]>,
  bestThirdGlobal: GroupStanding[],
  finishedKoByMatch: Record<number, FinishedKoGame>
  ): string {
  switch (seed.type) {
  case 'GROUP_POSITION': {
    const rows = standingsByGroup[seed.group];
    if (!rows) return `${seed.position}${seed.group}`;
    const row = rows.find((r) => r.position === seed.position);
    return row?.teamId || `${seed.position}${seed.group}`;
  }

case 'BEST_THIRD': {
  const entry = bestThirdGlobal[seed.rank - 1];
  return entry?.teamId || `3rd_${seed.rank}`;
}

case 'WINNER_OF_MATCH': {
  const g = finishedKoByMatch[seed.matchNumber];
  if (!g) return `W${seed.matchNumber}`;
  const winner = getWinner(g);
  return winner || `W${seed.matchNumber}`;
}

case 'LOSER_OF_MATCH': {
  const g = finishedKoByMatch[seed.matchNumber];
  if (!g) return `L${seed.matchNumber}`;
  const loser = getLoser(g);
  return loser || `L${seed.matchNumber}`;
}

default:
  return 'TBD';
}
}

async function materializeKnockoutGames(tournamentId: string): Promise<void> {
  const standingsSnap = await db
  .collection(STANDINGS_COLLECTION)
  .where('tournamentId', '==', tournamentId)
  .get();

  const standingsByGroup: Record<string, GroupStanding[]> = {};
  let bestThirdGlobal: GroupStanding[] = [];

  for (const doc of standingsSnap.docs) {
    const data = doc.data();
    if (doc.id.endsWith('bestThirds')) {
      bestThirdGlobal = (data.bestThirds ?? []) as GroupStanding[];
    } else if (data.group) {
      standingsByGroup[String(data.group)] = (data.table ?? []) as GroupStanding[];
    }
  }

  const koGamesSnap = await db
  .collection(GAMES_COLLECTION)
  .where('tournamentId', '==', tournamentId)
  .where('status', '==', 'FINISHED')
  .get();

  const finishedKoByMatch: Record<number, FinishedKoGame> = {};
  for (const doc of koGamesSnap.docs) {
    const d = doc.data();
    const mn = d.matchNumber as number | undefined;
    if (typeof mn !== 'number' || mn < 73) continue;
    if (typeof d.homeGoals !== 'number' || typeof d.awayGoals !== 'number') continue;

    finishedKoByMatch[mn] = {
      matchNumber: mn,
      homeTeamId: String(d.homeTeamId ?? ''),
      awayTeamId: String(d.awayTeamId ?? ''),
      homeGoals: d.homeGoals as number,
      awayGoals: d.awayGoals as number,
      wentToPenalties: !!d.wentToPenalties,
      homePenalties: d.homePenalties ?? null,
      awayPenalties: d.awayPenalties ?? null,
    };
  }

  const batch = db.batch();

  for (const slot of ALL_KNOCKOUT_SLOTS) {
    const homeTeamId = resolveTeamId(
      slot.homeSource,
      standingsByGroup,
      bestThirdGlobal,
      finishedKoByMatch
      );
    const awayTeamId = resolveTeamId(
      slot.awaySource,
      standingsByGroup,
      bestThirdGlobal,
      finishedKoByMatch
      );

    const docId = `${tournamentId}_match_${slot.matchNumber}`;
    const ref = db.collection(GAMES_COLLECTION).doc(docId);

    batch.set(
      ref,
      {
        tournamentId,
        matchNumber: slot.matchNumber,
        stage: slot.stage,
        homeTeamId,
        awayTeamId,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
      );
  }

  await batch.commit();
  logger.info(`Knockout games materialized for ${tournamentId}: ${ALL_KNOCKOUT_SLOTS.length} slots.`);
}

// ------------------------------------------------------
// Recomputación interna de standings
// ------------------------------------------------------

async function recomputeStandingsForTournamentInternal(tournamentId: string) {
  logger.info(`Recomputando standings para torneo ${tournamentId}`);

  const teamsSnap = await db
  .collection(TEAMS_COLLECTION)
  .where('tournamentId', '==', tournamentId)
  .get();

  if (teamsSnap.empty) {
    logger.warn(`No hay teams para tournamentId=${tournamentId}`);
  }

  const teams: TeamDoc[] = teamsSnap.docs.map((doc) => {
    const data = doc.data();
    return {
      id: doc.id,
      name: (data.name ?? data.teamName ?? doc.id) as string,
      group: (data.group ?? '') as string,
      flagCode: (data.flagCode ?? data.flag ?? '') as string,
    };
  })
  .sort((a, b) => a.id.localeCompare(b.id));

  const gamesSnap = await db
  .collection(GAMES_COLLECTION)
  .where('tournamentId', '==', tournamentId)
  .get();

  const games: GameDoc[] = gamesSnap.docs.map((doc) => {
    const data = doc.data();
    return {
      tournamentId,
      homeTeamId: (data.homeTeamId ?? data.team1) as string,
      awayTeamId: (data.awayTeamId ?? data.team2) as string,
      stage: (data.stage ?? 'GROUP') as MatchStage,
      group: (data.group ?? data.round ?? undefined) as string | undefined,
      homeGoals: data.homeGoals as number | undefined,
      awayGoals: data.awayGoals as number | undefined,
      status: (data.status ?? 'SCHEDULED') as string,
    };
  });

  const { standingsByGroup, bestThirdGlobal } = computeStandingsAndBestThirds(teams, games);

  const batch = db.batch();

  for (const group of Object.keys(standingsByGroup)) {
    const standings = standingsByGroup[group];
    const docId = `${tournamentId}_${group}`;
    const ref = db.collection(STANDINGS_COLLECTION).doc(docId);

    batch.set(
      ref,
      {
        tournamentId,
        group,
        table: standings,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
      );
  }

  const thirdsRef = db.collection(STANDINGS_COLLECTION).doc(`${tournamentId}_bestThirds`);

  batch.set(
    thirdsRef,
    {
      tournamentId,
      bestThirds: bestThirdGlobal,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
    );

  await batch.commit();

  logger.info(`Standings actualizados para torneo ${tournamentId} en ${STANDINGS_COLLECTION}.`);

  await materializeKnockoutGames(tournamentId);

  return {
    groups: Object.keys(standingsByGroup),
    thirdCount: bestThirdGlobal.length,
  };
}

// ------------------------------------------------------
// Callable: recalcular standings de un torneo
// ------------------------------------------------------

export const recomputeStandingsForTournament = onCall(
  { region: 'us-central1' },
  async (request) => {
    const tournamentId = request.data?.tournamentId as string | undefined;

    if (!tournamentId) {
      throw new HttpsError('invalid-argument', 'Debe enviar tournamentId en el payload.');
    }

    const result = await recomputeStandingsForTournamentInternal(tournamentId);

    return {
      ok: true,
      groups: result.groups,
      thirdCount: result.thirdCount,
    };
  }
  );

// ------------------------------------------------------
// Notificación: juego terminado
// ------------------------------------------------------

async function sendGameFinishedNotification(params: {
  gameId: string;
  tournamentId: string;
  matchNumber: number;
  homeTeamId: string;
  awayTeamId: string;
  homeGoals: number;
  awayGoals: number;
  wentToPenalties?: boolean;
  homePenalties?: number | null;
  awayPenalties?: number | null;
}) {
  const topic = params.tournamentId ? `results_${params.tournamentId}` : 'results_all';

  const title = 'Resultado final';
  let body = `${params.homeTeamId} ${params.homeGoals}-${params.awayGoals} ${params.awayTeamId}`;

  if (
    params.wentToPenalties &&
    typeof params.homePenalties === 'number' &&
    typeof params.awayPenalties === 'number'
    ) {
    body += ` (Penales ${params.homePenalties}-${params.awayPenalties})`;
}

await admin.messaging().send({
  topic,
  notification: { title, body },
  data: {
    type: 'GAME_FINISHED',
    gameId: params.gameId,
    matchNumber: String(params.matchNumber),
    tournamentId: params.tournamentId,
  },
});

logger.info(`Push sent: topic=${topic}, gameId=${params.gameId}, match=${params.matchNumber}`);
}

/**
 * Trigger: cuando se actualiza un partido en "games/{gameId}".
 * 1) Si pasa a FINISHED, envía notificación.
 * 2) Recalcula puntos de predicciones si existen.
 * 3) Si es fase de grupos, recalcula standings + playoff.
 * 4) Si es knockout, propaga ganador/perdedor a rondas siguientes.
 */
export const onGameResultUpdated = onDocumentUpdated(
{
  document: `${GAMES_COLLECTION}/{gameId}`,
  region: 'us-central1',
},
async (event) => {
  const beforeSnap = event.data?.before;
  const afterSnap = event.data?.after;

  if (!afterSnap) return;

  const before = beforeSnap?.data() ?? {};
  const after = afterSnap.data() ?? {};

  const actualHome = after.homeGoals;
  const actualAway = after.awayGoals;

  if (typeof actualHome !== 'number' || typeof actualAway !== 'number') {
    logger.info('Game without valid score, skipping.');
    return;
  }

  const statusBefore = before.status;
  const statusAfter = after.status;

  const homeBefore = before.homeGoals;
  const awayBefore = before.awayGoals;

  const finishedNow = statusAfter === 'FINISHED' && statusBefore !== 'FINISHED';
  const scoreChanged = homeBefore !== actualHome || awayBefore !== actualAway;

  if (!finishedNow && !scoreChanged) {
    logger.info('No finished status nor score change, skipping.');
    return;
  }

  const gameId = event.params.gameId as string;
  const matchNumber = after.matchNumber;
  const tournamentId = String(after.tournamentId ?? '');

  if (typeof matchNumber !== 'number') {
    logger.warn(`Game ${gameId} without matchNumber, skipping.`);
    return;
  }

  if (!tournamentId) {
    logger.warn(`Game ${gameId} without tournamentId, skipping to avoid cross-tournament pollution.`);
    return;
  }

  if (finishedNow) {
    if (after.resultNotifiedAt) {
      logger.info(`Game ${gameId} already notified, skipping push.`);
    } else {
      const homeTeamId = String(after.homeTeamId ?? '');
      const awayTeamId = String(after.awayTeamId ?? '');

      await sendGameFinishedNotification({
        gameId,
        tournamentId,
        matchNumber,
        homeTeamId,
        awayTeamId,
        homeGoals: actualHome,
        awayGoals: actualAway,
        wentToPenalties: !!after.wentToPenalties,
        homePenalties: after.homePenalties ?? null,
        awayPenalties: after.awayPenalties ?? null,
      });

      await afterSnap.ref.set(
        { resultNotifiedAt: admin.firestore.FieldValue.serverTimestamp() },
        { merge: true }
        );
    }
  }

  if (statusAfter !== 'FINISHED') {
    logger.info(
  `Game ${gameId} is not FINISHED (status=${String(statusAfter)}), skipping post-processing.`
  );
    return;
  }

  logger.info(
`Recomputing points for gameId=${gameId}, matchNumber=${matchNumber}, tournamentId=${tournamentId}`
);

  const predictionsSnap = await db
  .collection(PREDICTIONS_COLLECTION)
  .where('tournamentId', '==', tournamentId)
  .where('matchNumber', '==', matchNumber)
  .get();

  if (predictionsSnap.empty) {
    logger.info('No predictions for this game, skipping points recompute.');
  } else {
    const batch = db.batch();

    predictionsSnap.forEach((docSnap) => {
      const data = docSnap.data();

      const userId: string = String(data.userId ?? '');
      const predictedHome: number = Number(data.predictedHomeGoals);
      const predictedAway: number = Number(data.predictedAwayGoals);
      const oldPoints: number = Number(data.points ?? 0);

      if (!userId || !Number.isFinite(predictedHome) || !Number.isFinite(predictedAway)) {
        logger.warn(`Invalid prediction doc ${docSnap.id}, skipping.`);
        return;
      }

      const newPoints = computePoints(actualHome, actualAway, predictedHome, predictedAway);
      const delta = newPoints - oldPoints;

      batch.update(docSnap.ref, {
        points: newPoints,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      const userRef = db.collection(USERS_COLLECTION).doc(userId);
      batch.set(
        userRef,
        {
          tournamentId,
          points: admin.firestore.FieldValue.increment(delta),
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        },
        { merge: true }
        );
    });

    await batch.commit();
    logger.info('Points recomputed successfully for all predictions.');
  }

  if (matchNumber < 73) {
    logger.info(`Group game ${matchNumber} finished/changed, recomputing standings for ${tournamentId}`);
    await recomputeStandingsForTournamentInternal(tournamentId);
  } else {
    logger.info(`KO game ${matchNumber} finished/changed, materializing next-round brackets for ${tournamentId}`);
    await materializeKnockoutGames(tournamentId);
  }
}
);