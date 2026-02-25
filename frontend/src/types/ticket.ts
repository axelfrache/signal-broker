export interface Ticket {
    ticketId: string;
    subject: string;
    contact: string;
    confidence: number;
    receivedAt: number;
    labeledAt: number;
    category: string;
    priority: string;
    ticketType: string;
}

export interface TicketDetails extends Ticket {
    body: string;
    schemaVersion: number;
}

export interface PaginatedResponse<T> {
    items: T[];
    page: {
        page: number;
        size: number;
        totalItems: number;
        totalPages: number;
    };
}
