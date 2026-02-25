import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/card";
import { AlertCircle, Hash, TrendingUp } from "lucide-react";
import { api } from "../lib/api";
import type { StatsOverview } from "../types/stats";
import { Skeleton } from "../components/ui/skeleton";
import { Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis, Bar, BarChart } from "recharts";
import { format } from "date-fns";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/ui/select";

export default function DashboardPage() {
    const [stats, setStats] = useState<StatsOverview | null>(null);
    const [loading, setLoading] = useState(true);

    const [timeRange, setTimeRange] = useState("all");

    useEffect(() => {
        setLoading(true);
        let from: number | undefined;
        let to: number | undefined;
        const now = Math.floor(Date.now() / 1000);

        if (timeRange === "24h") {
            to = now;
            from = now - 86400;
        } else if (timeRange === "7d") {
            to = now;
            from = now - 7 * 86400;
        } else if (timeRange === "30d") {
            to = now;
            from = now - 30 * 86400;
        }

        api.stats.overview({ from, to })
            .then(setStats)
            .catch(console.error)
            .finally(() => setLoading(false));
    }, [timeRange]);

    if (loading || !stats) {
        return (
            <div className="space-y-6">
                <div className="flex items-start justify-between gap-4 mb-4">
                    <div>
                        <h1 className="text-2xl font-semibold">Dashboard</h1>
                        <p className="text-muted-foreground">Overview of labeled tickets and AI engine performance.</p>
                    </div>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
                    {[...Array(3)].map((_, i) => (
                        <Skeleton key={i} className="h-[120px] w-full" />
                    ))}
                </div>
                <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                    {[...Array(3)].map((_, i) => (
                        <Skeleton key={i} className={`h-[320px] w-full ${i === 2 ? 'xl:col-span-2' : ''}`} />
                    ))}
                </div>
            </div>
        );
    }

    const p0Count = stats.byPriority?.['P0'] || 0;

    // Prepare chart data
    const timeSeriesData = (stats.series || []).map(p => ({
        time: format(new Date(p.ts * 1000), "MMM d HH:mm"),
        count: p.count,
    }));

    const categoryData = Object.entries(stats.byCategory || {}).map(([name, value]) => ({ name, value }));
    const priorityData = Object.entries(stats.byPriority || {}).map(([name, value]) => ({ name, value }));

    return (
        <div className="space-y-6">
            <div className="flex items-start justify-between gap-4 mb-4">
                <div>
                    <h1 className="text-2xl font-semibold">Dashboard</h1>
                    <p className="text-muted-foreground">Overview of labeled tickets and AI engine performance.</p>
                </div>
                <Select value={timeRange} onValueChange={setTimeRange}>
                    <SelectTrigger className="w-[160px]">
                        <SelectValue placeholder="Time Range" />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="24h">Last 24 Hours</SelectItem>
                        <SelectItem value="7d">Last 7 Days</SelectItem>
                        <SelectItem value="30d">Last 30 Days</SelectItem>
                        <SelectItem value="all">All Time</SelectItem>
                    </SelectContent>
                </Select>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
                <Card className="h-[120px]">
                    <div className="p-5 flex items-start justify-between h-full">
                        <div className="space-y-1">
                            <p className="text-sm font-medium text-muted-foreground">Total Tickets</p>
                            <div className="text-3xl font-bold">{stats.totalTickets}</div>
                            <p className="text-xs text-muted-foreground">+{stats.last24hCount} from last 24h</p>
                        </div>
                        <Hash className="h-5 w-5 text-muted-foreground" />
                    </div>
                </Card>

                <Card className="h-[120px]">
                    <div className="p-5 flex items-start justify-between h-full">
                        <div className="space-y-1">
                            <p className="text-sm font-medium text-muted-foreground">Critical (P0)</p>
                            <div className="text-3xl font-bold text-destructive">{p0Count}</div>
                            <p className="text-xs text-muted-foreground">
                                {stats.totalTickets ? Math.round((p0Count / stats.totalTickets) * 100) : 0}% of total
                            </p>
                        </div>
                        <AlertCircle className="h-5 w-5 text-destructive" />
                    </div>
                </Card>

                <Card className="h-[120px]">
                    <div className="p-5 flex items-start justify-between h-full">
                        <div className="space-y-1">
                            <p className="text-sm font-medium text-muted-foreground">Avg Confidence</p>
                            <div className="text-3xl font-bold">{(stats.avgConfidence * 100).toFixed(1)}%</div>
                            <p className="text-xs text-muted-foreground">AI classification accuracy</p>
                        </div>
                        <TrendingUp className="h-5 w-5 text-muted-foreground" />
                    </div>
                </Card>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 items-stretch">
                <Card className="col-span-1 min-h-[320px] flex flex-col">
                    <CardHeader>
                        <CardTitle>Tickets Over Time</CardTitle>
                    </CardHeader>
                    <CardContent className="flex-1 w-full h-full flex items-center justify-center min-h-[250px] pb-6">
                        {timeSeriesData.length === 0 ? (
                            <p className="text-sm text-muted-foreground">No data available for this period.</p>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                <LineChart data={timeSeriesData}>
                                    <XAxis dataKey="time" stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
                                    <YAxis stroke="#888888" fontSize={12} tickLine={false} axisLine={false} tickFormatter={(value) => `${value}`} />
                                    <Tooltip />
                                    <Line type="monotone" dataKey="count" stroke="#0ea5e9" strokeWidth={2} dot={false} />
                                </LineChart>
                            </ResponsiveContainer>
                        )}
                    </CardContent>
                </Card>

                <Card className="col-span-1 min-h-[320px] flex flex-col">
                    <CardHeader>
                        <CardTitle>Distribution by Priority</CardTitle>
                    </CardHeader>
                    <CardContent className="flex-1 w-full h-full flex items-center justify-center min-h-[250px] pb-6">
                        {priorityData.length === 0 ? (
                            <p className="text-sm text-muted-foreground">No data available for this period.</p>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={priorityData}>
                                    <XAxis dataKey="name" stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
                                    <YAxis stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
                                    <Tooltip cursor={{ fill: 'transparent' }} />
                                    <Bar dataKey="value" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        )}
                    </CardContent>
                </Card>

                <Card className="xl:col-span-2 min-h-[320px] flex flex-col">
                    <CardHeader>
                        <CardTitle>Distribution by Category</CardTitle>
                    </CardHeader>
                    <CardContent className="flex-1 w-full h-full flex items-center justify-center min-h-[250px] pb-6">
                        {categoryData.length === 0 ? (
                            <p className="text-sm text-muted-foreground">No data available for this period.</p>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={categoryData} layout="vertical">
                                    <XAxis type="number" stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
                                    <YAxis dataKey="name" type="category" stroke="#888888" fontSize={12} tickLine={false} axisLine={false} width={100} />
                                    <Tooltip cursor={{ fill: 'transparent' }} />
                                    <Bar dataKey="value" fill="#10b981" radius={[0, 4, 4, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
