import { useEffect, useState, useRef } from 'react';
import { api } from '../../lib/api';
import type { TicketComment } from '../../types/comment';
import { formatDateTime } from '../../lib/format';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../ui/card';
import { Input } from '../ui/input';
import { Textarea } from '../ui/textarea';
import { Button } from '../ui/button';
import { Separator } from '../ui/separator';
import { Skeleton } from '../ui/skeleton';
import { AlertCircle, Send } from 'lucide-react';
import { Alert, AlertDescription } from '../ui/alert';

interface ThreadProps {
    ticketId: string;
}

export function TicketCommentsThread({ ticketId }: ThreadProps) {
    const [comments, setComments] = useState<TicketComment[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [authorName, setAuthorName] = useState('');
    const [body, setBody] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const endOfThreadRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        loadComments();
    }, [ticketId]);

    const loadComments = () => {
        setLoading(true);
        setError(null);
        api.tickets.getComments(ticketId)
            .then(data => {
                setComments(data);
                scrollToBottom();
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    };

    const scrollToBottom = () => {
        setTimeout(() => {
            endOfThreadRef.current?.scrollIntoView({ behavior: 'smooth' });
        }, 100);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!authorName.trim() || !body.trim()) return;

        setSubmitting(true);
        setError(null);
        try {
            const newComment = await api.tickets.addComment(ticketId, {
                authorName: authorName.trim(),
                body: body.trim()
            });
            setComments(prev => [...prev, newComment]);
            setBody('');
            scrollToBottom();
        } catch (err: any) {
            setError(err.message || 'Failed to post comment');
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <Card>
                <CardHeader>
                    <Skeleton className="h-6 w-48 mb-2" />
                    <Skeleton className="h-4 w-64" />
                </CardHeader>
                <CardContent className="space-y-4">
                    <Skeleton className="h-16 w-full" />
                    <Skeleton className="h-16 w-3/4" />
                </CardContent>
            </Card>
        );
    }

    return (
        <Card className="flex flex-col h-full max-h-[600px]">
            <CardHeader>
                <CardTitle>Comments</CardTitle>
                <CardDescription>Internal thread attached to this ticket.</CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col flex-1 overflow-hidden">
                {error && (
                    <Alert variant="destructive" className="mb-4">
                        <AlertCircle className="h-4 w-4" />
                        <AlertDescription>{error}</AlertDescription>
                    </Alert>
                )}

                <div className="flex-1 overflow-y-auto pr-2 space-y-4 mb-4">
                    {comments.length === 0 ? (
                        <p className="text-sm text-muted-foreground italic">No comments yet.</p>
                    ) : (
                        comments.map((c, i) => (
                            <div key={c.id}>
                                <div className="flex flex-col gap-1">
                                    <div className="flex items-center gap-2">
                                        <span className="font-semibold text-sm">{c.authorName}</span>
                                        <span className="text-xs text-muted-foreground">{formatDateTime(Date.parse(c.createdAt))}</span>
                                    </div>
                                    <p className="text-sm whitespace-pre-wrap mt-1 text-slate-700 dark:text-slate-300">{c.body}</p>
                                </div>
                                {i < comments.length - 1 && <Separator className="my-4" />}
                            </div>
                        ))
                    )}
                    <div ref={endOfThreadRef} />
                </div>

                <div className="pt-2">
                    <Separator className="mb-4" />
                    <form onSubmit={handleSubmit} className="flex flex-col gap-3">
                        <Input
                            placeholder="Your Name (e.g. Alice)"
                            value={authorName}
                            onChange={(e) => setAuthorName(e.target.value)}
                            disabled={submitting}
                            required
                            minLength={2}
                            maxLength={64}
                        />
                        <Textarea
                            placeholder="Write an internal comment..."
                            value={body}
                            onChange={(e) => setBody(e.target.value)}
                            disabled={submitting}
                            required
                            minLength={1}
                            maxLength={2000}
                            className="resize-none h-20"
                        />
                        <Button type="submit" disabled={submitting || !authorName.trim() || !body.trim()} className="self-end">
                            <Send className="h-4 w-4 mr-2" />
                            Post Comment
                        </Button>
                    </form>
                </div>
            </CardContent>
        </Card>
    );
}
