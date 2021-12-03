import { Continue } from "./continue";
import { Query } from "./query";

export class Result {
    batchcomplete!: String;
    continue!: Continue;
    query!: Query; 
}
