package guess.domain.source.contentful.talk;

import guess.domain.source.contentful.ContentfulEntity;
import guess.domain.source.contentful.talk.fields.ContentfulTalkFields;

public class ContentfulTalk<T extends ContentfulTalkFields> extends ContentfulEntity {
    private T fields;

    public T getFields() {
        return fields;
    }

    public void setFields(T fields) {
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
