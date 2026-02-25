import { useEffect, useState } from "react";
import { api } from "../lib/api";
import type { PaginatedResponse, Ticket } from "../types/ticket";
import { formatDateTime, getPriorityVariant } from "../lib/format";
import { Badge } from "../components/ui/badge";
import { Input } from "../components/ui/input";
import { Button } from "../components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../components/ui/table";
import { Skeleton } from "../components/ui/skeleton";
import { useNavigate } from "react-router-dom";
import { Search, RefreshCw, ChevronLeft, ChevronRight } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/ui/select";

export default function TicketsPage() {
    const navigate = useNavigate();
    const [data, setData] = useState<PaginatedResponse<Ticket> | null>(null);
    const [loading, setLoading] = useState(true);

    // Filters
    const [page, setPage] = useState(0);
    const [q, setQ] = useState("");
    const [searchInput, setSearchInput] = useState("");
    const [priority, setPriority] = useState<string>("ALL");
    const [category, setCategory] = useState<string>("ALL");
    const size = 15;

    const fetchTickets = () => {
        setLoading(true);
        api.tickets.list({
            page,
            size,
            q: q || undefined,
            priority: priority !== "ALL" ? [priority] : undefined,
            category: category !== "ALL" ? [category] : undefined
        })
            .then(setData)
            .catch(console.error)
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        fetchTickets();
    }, [page, q, priority, category]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setPage(0);
        setQ(searchInput);
    };

    return (
        <div className="space-y-6">
            <div className="flex items-start justify-between gap-4 mb-4">
                <div>
                    <h1 className="text-2xl font-semibold">Tickets</h1>
                    <p className="text-muted-foreground">Manage and filter your labeled support tickets.</p>
                </div>
                <Button variant="ghost" size="sm" onClick={fetchTickets} disabled={loading}>
                    <RefreshCw className={`mr-2 h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                    Refresh
                </Button>
            </div>

            <div className="flex flex-col sm:flex-row gap-4 items-center">
                <form onSubmit={handleSearch} className="flex-1 flex gap-2 w-full">
                    <Input
                        placeholder="Search subject, body or contact..."
                        value={searchInput}
                        onChange={(e) => setSearchInput(e.target.value)}
                        className="max-w-sm"
                    />
                    <Button type="submit" variant="secondary" size="icon">
                        <Search className="h-4 w-4" />
                    </Button>
                </form>

                <Select value={priority} onValueChange={(v) => { setPriority(v); setPage(0); }}>
                    <SelectTrigger className="w-[140px]">
                        <SelectValue placeholder="Priority" />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="ALL">All Priorities</SelectItem>
                        <SelectItem value="P0">P0 - Critical</SelectItem>
                        <SelectItem value="P1">P1 - High</SelectItem>
                        <SelectItem value="P2">P2 - Medium</SelectItem>
                        <SelectItem value="P3">P3 - Low</SelectItem>
                    </SelectContent>
                </Select>

                <Select value={category} onValueChange={(v) => { setCategory(v); setPage(0); }}>
                    <SelectTrigger className="w-[160px]">
                        <SelectValue placeholder="Category" />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="ALL">All Categories</SelectItem>
                        <SelectItem value="FRONTEND">FRONTEND</SelectItem>
                        <SelectItem value="BACKEND">BACKEND</SelectItem>
                        <SelectItem value="INFRA">INFRA</SelectItem>
                        <SelectItem value="MOBILE">MOBILE</SelectItem>
                    </SelectContent>
                </Select>
            </div>

            <div className="rounded-md border bg-white dark:bg-slate-950">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[100px]">Priority</TableHead>
                            <TableHead className="w-[120px]">Category</TableHead>
                            <TableHead>Subject</TableHead>
                            <TableHead>Contact</TableHead>
                            <TableHead className="text-right">Confidence</TableHead>
                            <TableHead className="text-right">Received</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {loading && !data ? (
                            [...Array(5)].map((_, i) => (
                                <TableRow key={i}>
                                    <TableCell><Skeleton className="h-6 w-12" /></TableCell>
                                    <TableCell><Skeleton className="h-6 w-20" /></TableCell>
                                    <TableCell><Skeleton className="h-4 w-48" /></TableCell>
                                    <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                                    <TableCell className="text-right"><Skeleton className="h-4 w-12 ml-auto" /></TableCell>
                                    <TableCell className="text-right"><Skeleton className="h-4 w-24 ml-auto" /></TableCell>
                                </TableRow>
                            ))
                        ) : data?.items.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={6} className="h-24 text-center text-muted-foreground">
                                    No tickets found.
                                </TableCell>
                            </TableRow>
                        ) : data?.items.map((ticket) => (
                            <TableRow
                                key={ticket.ticketId}
                                className="cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-900"
                                onClick={() => navigate(`/tickets/${ticket.ticketId}`)}
                            >
                                <TableCell>
                                    <Badge variant={getPriorityVariant(ticket.priority)}>
                                        {ticket.priority || 'N/A'}
                                    </Badge>
                                </TableCell>
                                <TableCell>
                                    <Badge variant="secondary" className="font-mono text-xs">
                                        {ticket.category || 'N/A'}
                                    </Badge>
                                </TableCell>
                                <TableCell className="font-medium max-w-[300px] truncate">
                                    {ticket.subject || 'No Subject'}
                                </TableCell>
                                <TableCell className="text-muted-foreground text-sm truncate max-w-[200px]">
                                    {ticket.contact || 'Unknown'}
                                </TableCell>
                                <TableCell className="text-right font-mono text-xs text-muted-foreground">
                                    {(ticket.confidence * 100).toFixed(1)}%
                                </TableCell>
                                <TableCell className="text-right text-sm text-muted-foreground whitespace-nowrap">
                                    {formatDateTime(ticket.receivedAt)}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>

            {data && data.page.totalPages > 1 && (
                <div className="flex items-center justify-between">
                    <p className="text-sm text-muted-foreground">
                        Showing {data.page.page * data.page.size + 1} to {Math.min((data.page.page + 1) * data.page.size, data.page.totalItems)} of {data.page.totalItems} entries
                    </p>
                    <div className="flex gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setPage(p => Math.max(0, p - 1))}
                            disabled={page === 0 || loading}
                        >
                            <ChevronLeft className="h-4 w-4 mr-1" /> Previous
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setPage(p => p + 1)}
                            disabled={page >= data.page.totalPages - 1 || loading}
                        >
                            Next <ChevronRight className="h-4 w-4 ml-1" />
                        </Button>
                    </div>
                </div>
            )}
        </div>
    );
}
