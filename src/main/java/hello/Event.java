// tag::sample[]
package hello;

import javax.persistence.*;
import java.sql.Blob;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String text;

    @Lob
    @Column
    private Blob data;

    protected Event() {}

    public Event(String text) {
        this(text, null);
    }

    public Event(String text, Blob data) {
        this.text = text;
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format(
                "Event[id=%d, text='%s']",
                id, text);
    }

// end::sample[]

	public Long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

    public Blob getData() {
        return data;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setData(Blob data) {
        this.data = data;
    }
}

