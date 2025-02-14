import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Event } from '../models/event/event.model';
import { EventPart } from '../models/event/event-part.model';
import { EventType } from '../models/event-type/event-type.model';
import { EventDetails } from '../models/event/event-details.model';
import { Organizer } from '../models/organizer/organizer.model';
import { MessageService } from '../../modules/message/message.service';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private baseUrl = 'api/event';

  constructor(private http: HttpClient, private messageService: MessageService) {
  }

  getEventHttpParams(isConferences: boolean, isMeetups: boolean, organizer: Organizer, eventType: EventType): HttpParams {
    let params = new HttpParams()
      .set('conferences', isConferences.toString())
      .set('meetups', isMeetups.toString());
    if (organizer) {
      params = params.set('organizerId', organizer.id.toString());
    }
    if (eventType) {
      params = params.set('eventTypeId', eventType.id.toString());
    }

    return params;
  }

  getEvents(isConferences: boolean, isMeetups: boolean, organizer: Organizer, eventType: EventType): Observable<Event[]> {
    const params = this.getEventHttpParams(isConferences, isMeetups, organizer, eventType);

    return this.http.get<Event[]>(`${this.baseUrl}/events`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getEventParts(isConferences: boolean, isMeetups: boolean, organizer: Organizer, eventType: EventType): Observable<EventPart[]> {
    const params = this.getEventHttpParams(isConferences, isMeetups, organizer, eventType);

    return this.http.get<EventPart[]>(`${this.baseUrl}/event-parts`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getDefaultEvent(): Observable<Event> {
    return this.http.get<Event>(`${this.baseUrl}/default-event`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getDefaultEventPartHomeInfo(): Observable<EventPart> {
    return this.http.get<EventPart>(`${this.baseUrl}/default-event-part-home-info`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getEvent(id: number): Observable<EventDetails> {
    return this.http.get<EventDetails>(`${this.baseUrl}/event/${id}`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }
}
