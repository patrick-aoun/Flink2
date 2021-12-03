export class Query {
    searchinfo!: Searchinfo;
    search!: Search[];
}

export interface Searchinfo {
    totalhits: number;
    suggestion: string;
    suggestionsnippet: string;
}

export interface Search {
    ns: number;
    title: string;
    pageid: number;
    size: number;
    wordcount: number;
    snippet: string;
    timestamp: Date;
}