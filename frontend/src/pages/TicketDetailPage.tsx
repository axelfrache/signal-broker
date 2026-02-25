import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { api } from "../lib/api";
import type { TicketDetails } from "../types/ticket";
import { formatDateTime, getPriorityVariant } from "../lib/format";
import { Badge } from "../components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../components/ui/card";
import { Skeleton } from "../components/ui/skeleton";
import { Button } from "../components/ui/button";
import { ArrowLeft, Clock, User, Fingerprint, Tag, Code2, AlertTriangle } from "lucide-react";
import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from "../components/ui/accordion";
import { TicketCommentsThread } from "../components/tickets/TicketCommentsThread";

export default function TicketDetailPage() {
    const { ticketId } = useParams<{ ticketId: string }>();
    const navigate = useNavigate();
    const [ticket, setTicket] = useState<TicketDetails | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        if (!ticketId) return;
        setLoading(true);
        api.tickets.get(ticketId)
            .then(setTicket)
            .catch((err) => setError(err.message || "Failed to load ticket"))
            .finally(() => setLoading(false));
    }, [ticketId]);

    if (loading) {
        return (
            <div className="space-y-6">
                <Button variant="ghost" className="-ml-4"><ArrowLeft className="mr-2 h-4 w-4" /> Back</Button>
                <Skeleton className="h-12 w-3/4" />
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <Skeleton className="h-[400px] md:col-span-2" />
                    <Skeleton className="h-[400px]" />
                </div>
            </div>
        );
    }

    if (error || !ticket) {
        return (
            <div className="space-y-6">
                <Button variant="ghost" onClick={() => navigate('/tickets')} className="-ml-4"><ArrowLeft className="mr-2 h-4 w-4" /> Back to tickets</Button>
                <div className="bg-destructive/15 text-destructive border-destructive/20 border p-4 rounded-md flex items-center gap-3">
                    <AlertTriangle className="h-5 w-5" />
                    <p>{error || "Ticket not found"}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div>
                <Button variant="ghost" onClick={() => navigate(-1)} className="-ml-4 text-muted-foreground hover:text-foreground">
                    <ArrowLeft className="mr-2 h-4 w-4" /> Back
                </Button>
            </div>

            <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight mb-2">{ticket.subject}</h1>
                    <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
                        <Badge variant={getPriorityVariant(ticket.priority)}>
                            {ticket.priority || 'N/A'}
                        </Badge>
                        <Badge variant="secondary" className="font-mono">
                            {ticket.category || 'N/A'}
                        </Badge>
                        <span>•</span>
                        <span className="flex items-center gap-1">
                            <Clock className="h-3.5 w-3.5" />
                            {formatDateTime(ticket.receivedAt)}
                        </span>
                        <span>•</span>
                        <span className="flex items-center gap-1 font-mono">
                            <Code2 className="h-3.5 w-3.5" />
                            v{ticket.schemaVersion}
                        </span>
                    </div>
                </div>
                <div className="flex flex-col items-end gap-1 text-sm bg-slate-100 dark:bg-slate-900 px-4 py-2 rounded-lg border">
                    <span className="text-muted-foreground uppercase tracking-widest text-[10px] font-bold">AI Confidence</span>
                    <span className="font-mono text-xl font-medium tracking-tight">{(ticket.confidence * 100).toFixed(1)}%</span>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="md:col-span-2 space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Ticket Details</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="bg-slate-50 dark:bg-slate-900 border rounded-md p-4 text-sm whitespace-pre-wrap font-mono relative">
                                {ticket.body || <span className="text-muted-foreground italic">No content provided.</span>}
                            </div>
                        </CardContent>
                    </Card>

                    <Accordion type="single" collapsible className="w-full">
                        <AccordionItem value="raw-json">
                            <AccordionTrigger className="text-sm font-medium">View Raw JSON (Debug)</AccordionTrigger>
                            <AccordionContent>
                                <div className="bg-slate-950 text-slate-50 p-4 rounded-md overflow-auto text-xs font-mono">
                                    <pre>{JSON.stringify(ticket, null, 2)}</pre>
                                </div>
                            </AccordionContent>
                        </AccordionItem>
                    </Accordion>

                    <TicketCommentsThread ticketId={ticket.ticketId} />
                </div>

                <div className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Metadata</CardTitle>
                            <CardDescription>Associated ticket information</CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="space-y-1">
                                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                    <User className="h-4 w-4" /> Contact
                                </div>
                                <p className="text-sm font-medium">{ticket.contact || 'Unknown'}</p>
                            </div>

                            <div className="space-y-1">
                                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                    <Tag className="h-4 w-4" /> Ticket Type
                                </div>
                                <p className="text-sm font-medium">{ticket.ticketType || 'N/A'}</p>
                            </div>

                            <div className="space-y-1">
                                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                    <Fingerprint className="h-4 w-4" /> ID
                                </div>
                                <p className="text-xs font-mono break-all bg-slate-100 dark:bg-slate-900 p-1.5 rounded">{ticket.ticketId}</p>
                            </div>

                            <div className="space-y-1">
                                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                    <Clock className="h-4 w-4" /> Labeled At
                                </div>
                                <p className="text-sm font-medium">{formatDateTime(ticket.labeledAt)}</p>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </div>
        </div>
    );
}
