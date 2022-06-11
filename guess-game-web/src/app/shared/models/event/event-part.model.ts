import { AbstractEvent } from './abstract-event.model';

export class EventPart extends AbstractEvent {
  constructor(
    public startDate?: Date,
    public endDate?: Date,
    public placeCity?: string,
    public placeVenueAddress?: string,
    public mapCoordinates?: string
  ) {
    super();
  }
}
