import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry, catchError } from 'rxjs/operators';
import { Configs, handleError } from 'src/app/_configs/configs';
import { Result } from 'src/app/_shared/models/result';
@Injectable({
  providedIn: 'root'
})
export class SearchService {

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  }
  constructor(private http: HttpClient) { }

  // Fetching data from the api
  get(text: String): Observable<Result>{
    return this.http.get<Result>(Configs.apiUrl + text, this.httpOptions)
      .pipe(
        retry(1),
        catchError(handleError)
      )
  }
}
