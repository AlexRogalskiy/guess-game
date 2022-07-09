package guess.domain.source.cms.contentful.speaker;

import guess.domain.source.cms.contentful.ContentfulEntity;

public class ContentfulSpeaker extends ContentfulEntity {
    private ContentfulSpeakerFields fields;

    public ContentfulSpeakerFields getFields() {
        return fields;
    }

    public void setFields(ContentfulSpeakerFields fields) {
        this.fields = fields;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
