/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hello;

import org.hibernate.engine.jdbc.BlobProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration("classpath:META-INF/spring/applicationContext-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class EventRepositoryTests {

    @Autowired
    private EventRepository events;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void testFindByText() {
        Event event = new Event("foo");
        event = events.save(event);

        List<Event> findByText = events.findByText(event.getText());

        assertThat(findByText).extracting(Event::getText).containsOnly(event.getText());
    }

    @Test
    public void testBlobViaSpringData() throws SQLException {
        Event event = new Event(
                "foo",
                BlobProxy.generateProxy(encodeString("bar"))
        );
        Long id = events.save(event).getId();

        entityManager.flush();
        entityManager.clear();

        event = events.getOne(id);
        event.setText("bazz");
        //event.setData(BlobProxy.generateProxy(event.getData().getBytes(1, Long.valueOf(event.getData().length()).intValue())));
        events.save(event);

        entityManager.flush();
        entityManager.clear();

        event = events.getOne(id);
        assertThat(decodeString(toBytes(event.getData()))).isEqualTo("bar");
        assertThat(event.getText()).isEqualTo("bazz");
    }

    @Test
    public void testBlobViaJpa() {
        Event event = new Event(
                "foo",
                BlobProxy.generateProxy(encodeString("bar"))
        );
        entityManager.persist(event);
        Long id = event.getId();

        entityManager.flush();
        entityManager.clear();

        event = entityManager.find(Event.class, id);
        event.setText("bazz");
        entityManager.persist(event);

        entityManager.flush();
        entityManager.clear();

        event = entityManager.find(Event.class, id);
        assertThat(decodeString(toBytes(event.getData()))).isEqualTo("bar");
        assertThat(event.getText()).isEqualTo("bazz");
    }

    private byte[] toBytes(Blob blob) {
        try {
            return blob.getBytes(1, Long.valueOf(blob.length()).intValue());
        } catch (SQLException e) {
            throw new RuntimeException("Error extracting bytes from Blob", e);
        }
    }

    private static byte[] encodeString(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static String decodeString(byte[] d) {
        return new String(d, StandardCharsets.UTF_8);
    }
}