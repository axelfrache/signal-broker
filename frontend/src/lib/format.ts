import { format } from 'date-fns';

export function formatDateTime(ts: number): string {
    if (!ts) return '';
    // Check if it's seconds or milliseconds
    const date = new Date(ts > 1e12 ? ts : ts * 1000);
    return format(date, 'MMM d, yyyy HH:mm');
}

export function getPriorityVariant(priority: string): 'default' | 'destructive' | 'secondary' | 'outline' {
    switch (priority?.toUpperCase()) {
        case 'P0': return 'destructive';
        case 'P1': return 'default';
        case 'P2':
        case 'P3': return 'outline';
        default: return 'secondary';
    }
}
