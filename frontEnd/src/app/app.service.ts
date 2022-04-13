import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders} from '@angular/common/http';
import {RequestOptions} from 'http';

@Injectable({
  providedIn: 'root'
})
export class AppService {

  private url= 'https://psyched-signal-345109.uc.r.appspot.com/fullDocumentProcessing';
  // private url1= 'http://localhost:8080/fullDocumentProcessing';

  constructor(private httpClient: HttpClient) { }

  passFile(fileValue: Object): Observable<Object> {
    let headers = new HttpHeaders({});
    headers.append('Content-Type', 'multipart/form-data');
    headers.append('Accept', 'application/pdf');
   let options = {
      headers: headers
   }
    return this.httpClient.post(`${this.url}`, fileValue ,options);
  }
}
