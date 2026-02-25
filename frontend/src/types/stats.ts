export interface TimeSeriesPoint {
    ts: number;
    count: number;
}

export interface StatsOverview {
    totalTickets: number;
    byPriority: Record<string, number>;
    byCategory: Record<string, number>;
    avgConfidence: number;
    last24hCount: number;
    series: TimeSeriesPoint[];
}
