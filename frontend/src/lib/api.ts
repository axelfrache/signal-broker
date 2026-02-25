import type { Ticket, TicketDetails, PaginatedResponse } from '../types/ticket';
import type { StatsOverview } from '../types/stats';
import type { TicketComment, CreateTicketCommentRequest } from '../types/comment';

const API_BASE = 'http://localhost:8080/api';

export const api = {
    tickets: {
        list: async (params?: { page?: number; size?: number; sort?: string; dir?: string; priority?: string[]; category?: string[]; q?: string; from?: number; to?: number }): Promise<PaginatedResponse<Ticket>> => {
            const url = new URL(`${API_BASE}/tickets`);
            if (params) {
                Object.entries(params).forEach(([key, value]) => {
                    if (value !== undefined && value !== null && value !== '') {
                        if (Array.isArray(value)) {
                            value.forEach(v => url.searchParams.append(key, v));
                        } else {
                            url.searchParams.append(key, String(value));
                        }
                    }
                });
            }
            const res = await fetch(url.toString());
            if (!res.ok) throw new Error('Failed to fetch tickets');
            return res.json();
        },
        get: async (id: string): Promise<TicketDetails> => {
            const res = await fetch(`${API_BASE}/tickets/${id}`);
            if (!res.ok) throw new Error('Failed to fetch ticket ' + id);
            return res.json();
        },
        getComments: async (id: string): Promise<TicketComment[]> => {
            const res = await fetch(`${API_BASE}/tickets/${id}/comments`);
            if (!res.ok) throw new Error('Failed to fetch ticket comments');
            return res.json();
        },
        addComment: async (id: string, req: CreateTicketCommentRequest): Promise<TicketComment> => {
            const res = await fetch(`${API_BASE}/tickets/${id}/comments`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(req)
            });
            if (!res.ok) throw new Error('Failed to create ticket comment');
            return res.json();
        }
    },
    stats: {
        overview: async (params?: { from?: number; to?: number }): Promise<StatsOverview> => {
            const url = new URL(`${API_BASE}/stats/overview`);
            if (params) {
                if (params.from) url.searchParams.append('from', params.from.toString());
                if (params.to) url.searchParams.append('to', params.to.toString());
            }
            const res = await fetch(url.toString());
            if (!res.ok) throw new Error('Failed to fetch stats');
            return res.json();
        }
    }
};
