export type TicketComment = {
    id: string; // UUID
    ticketId: string; // UUID
    authorName: string;
    body: string;
    createdAt: string; // ISO string
};

export type CreateTicketCommentRequest = {
    authorName: string;
    body: string;
};
