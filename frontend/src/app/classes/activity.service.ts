import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ClassActivity } from './classes.models';

@Injectable({
  providedIn: 'root'
})
export class ActivityService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/activities`;

  /** Disciplinas disponibles para asignar a una clase. */
  getActivities(): Observable<ClassActivity[]> {
    return this.http.get<ClassActivity[]>(this.baseUrl);
  }
}
