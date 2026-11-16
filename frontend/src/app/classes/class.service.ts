import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { GymClass } from './classes.models';

@Injectable({
  providedIn: 'root'
})
export class ClassService {
  private readonly http = inject(HttpClient);
  // environment.apiUrl ya incluye "/api"
  private readonly baseUrl = `${environment.apiUrl}/gym-classes`;

  /**
   * Lista las clases activas de la grilla horaria.
   * @param date "YYYY-MM-DD". Cuando el back soporte el parámetro calcula los cupos de esa fecha;
   *             hasta entonces lo ignora.
   */
  getClasses(date?: string): Observable<GymClass[]> {
    let params = new HttpParams();
    if (date) {
      params = params.set('date', date);
    }
    return this.http.get<GymClass[]>(this.baseUrl, { params });
  }
}
