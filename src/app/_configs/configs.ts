import { FormGroup } from '@angular/forms';
import { throwError } from 'rxjs';

// Config the api route to get the specific data
export class Configs {
    private static APIURL = 'https://en.wikipedia.org/w/api.php?action=query&list=search&prop=info&inprop=url&utf8=&format=json&origin=*&srlimit=20&srsearch=';
    public static get apiUrl() {
        return this.APIURL;
    }
}

// error handling
export function handleError(error: any) {
    let errorMessage = '';
    if (error.error instanceof ErrorEvent) {
        errorMessage = error.error.message;
    } else {
        errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    return throwError(errorMessage);
}

function delay(ms: number) {
    return new Promise( resolve => setTimeout(resolve, ms) );
}