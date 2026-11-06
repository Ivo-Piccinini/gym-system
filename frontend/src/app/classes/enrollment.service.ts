import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Enrollment } from './classes.models';

@Injectable({
  providedIn: 'root'
})
export class EnrollmentService {
  private readonly http = inject(HttpClient);
  // environment.apiUrl ya incluye "/api"
  private readonly baseUrl = `${environment.apiUrl}/enrollments`;

  /** Inscripciones del cliente logueado (solo rol CLIENT). */
  getMyEnrollments(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.baseUrl}/my-enrollments`);
  }

  /** Inscribe al cliente logueado a una clase. El back responde 201 con la inscripción creada. */
  enroll(classExternalId: string): Observable<Enrollment> {
    return this.http.post<Enrollment>(`${this.baseUrl}/class/${classExternalId}`, {});
  }

  /** Cancela una inscripción propia. El back responde 204 sin cuerpo. */
  cancel(enrollmentExternalId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${enrollmentExternalId}`);
  }
}
