package com.devit.mscore.db.mongo;

import static com.devit.mscore.util.AttributeConstants.getId;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.function.Consumer;

import com.devit.mscore.exception.DataDuplicationException;
import com.devit.mscore.exception.DataException;
import com.mongodb.DuplicateKeyException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class MongoRepositoryTest {

    private MongoRepository repository;

    private MongoCollection<Document> collection;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        var document = mock(Document.class);
        doReturn("id").when(document).get("name");
        var mongoCursor = mock(MongoCursor.class);
        doReturn(document).when(mongoCursor).next();
        doReturn(true, false).when(mongoCursor).hasNext();
        var listIndeces = mock(ListIndexesIterable.class);
        doReturn(mongoCursor).when(listIndeces).iterator();

        this.collection = mock(MongoCollection.class);
        doReturn(listIndeces).when(this.collection).listIndexes();

        this.repository = new MongoRepository(this.collection, List.of("id", "code"));
    }

    @Test
    public void testSave() throws DataException, JSONException {
        var foundDocument = mock(Document.class);
        var foundId = new ObjectId("603aed537004b70c47368fd9");
        doReturn(foundId).when(foundDocument).get("_id");
        var foundJsonString = "{\"id\":\"603aed537004b70c47368fd9\",\"domain\":\"domain\",\"name\":\"name\"}";
        doReturn(foundJsonString).when(foundDocument).toJson();
        var findResult = mock(FindIterable.class);
        doReturn(foundDocument).when(findResult).first();
        doReturn(findResult).when(this.collection).find(any(Document.class));

        var updateResult = mock(UpdateResult.class);
        doReturn(true).when(updateResult).wasAcknowledged();
        doReturn(updateResult).when(this.collection).replaceOne(any(Document.class), any(Document.class),
                any(ReplaceOptions.class));

        var json = "{\"id\":\"603aed537004b70c47368fd9\",\"domain\":\"domain\",\"name\":\"name\"}";
        var result = this.repository.save(new JSONObject(json));

        assertNotNull(result);
        assertThat(getId(result), is("603aed537004b70c47368fd9"));
    }

    @Test
    public void testSave_Create() throws DataException, JSONException {
        var findResult = mock(FindIterable.class);
        doReturn(null).when(findResult).first();
        doReturn(findResult).when(this.collection).find(any(Document.class));

        var updateResult = mock(UpdateResult.class);
        doReturn(true).when(updateResult).wasAcknowledged();
        doReturn(updateResult).when(this.collection).replaceOne(any(Document.class), any(Document.class),
                any(ReplaceOptions.class));

        var json = "{\"domain\":\"domain\",\"name\":\"name\"}";
        var result = this.repository.save(new JSONObject(json));

        assertNotNull(result);
        assertTrue(StringUtils.isNotBlank(getId(result)));
    }

    @Test
    public void testSave_DuplicatedKey() throws DataException, JSONException {
        var foundDocument = mock(Document.class);
        var foundId = new ObjectId("603aed537004b70c47368fd9");
        doReturn(foundId).when(foundDocument).get("_id");
        var foundJsonString = "{\"id\":\"603aed537004b70c47368fd9\",\"domain\":\"domain\",\"name\":\"name\"}";
        doReturn(foundJsonString).when(foundDocument).toJson();
        var findResult = mock(FindIterable.class);
        doReturn(foundDocument).when(findResult).first();
        doReturn(findResult).when(this.collection).find(any(Document.class));

        doThrow(mock(DuplicateKeyException.class)).when(this.collection).replaceOne(any(Document.class),
                any(Document.class), any(ReplaceOptions.class));

        var json = "{\"id\":\"603aed537004b70c47368fd9\",\"domain\":\"domain\",\"name\":\"name\"}";

        var ex = assertThrows(DataDuplicationException.class, () -> this.repository.save(new JSONObject(json)));
        assertThat(ex.getMessage(), is("Key is duplicated"));
        assertThat(ex.getCause(), instanceOf(DuplicateKeyException.class));
    }

    @Test
    public void testSave_ThrowsGeneralException() throws DataException, JSONException {
        var foundDocument = mock(Document.class);
        var foundId = new ObjectId("603aed537004b70c47368fd9");
        doReturn(foundId).when(foundDocument).get("_id");
        var foundJsonString = "{\"id\":\"603aed537004b70c47368fd9\",\"domain\":\"domain\",\"name\":\"name\"}";
        doReturn(foundJsonString).when(foundDocument).toJson();
        var findResult = mock(FindIterable.class);
        doReturn(foundDocument).when(findResult).first();
        doReturn(findResult).when(this.collection).find(any(Document.class));

        doThrow(new IllegalArgumentException("Exception message")).when(this.collection).replaceOne(any(Document.class),
                any(Document.class), any(ReplaceOptions.class));

        var json = "{\"id\":\"603aed537004b70c47368fd9\",\"domain\":\"domain\",\"name\":\"name\"}";
        var ex = assertThrows(DataException.class, () -> this.repository.save(new JSONObject(json)));
        assertThat(ex.getMessage(), is("Exception message"));
        assertThat(ex.getCause(), instanceOf(Exception.class));
    }

    @Test
    public void testDelete() {
        this.repository.delete("603aed537004b70c47368fd9");
        verify(this.collection).deleteOne(any(Document.class));
    }

    @Test
    public void testFindId_Empty() {
        var findResult = mock(FindIterable.class);
        doReturn(null).when(findResult).first();
        doReturn(findResult).when(this.collection).find(any(Document.class));

        var result = this.repository.find("603aed537004b70c47368fd9");
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindField() {
        var foundDocument = mock(Document.class);
        var foundId = new ObjectId("603aed537004b70c47368fd9");
        doReturn(foundId).when(foundDocument).get("_id");
        var foundJsonString = "{\"id\":\"603aed537004b70c47368fd9\",\"domain\":\"domain\",\"name\":\"name\"}";
        doReturn(foundJsonString).when(foundDocument).toJson();

        var iterator = mock(MongoCursor.class);
        doReturn(true, true, false).when(iterator).hasNext();
        doReturn(foundDocument).when(iterator).next();
        var findResult = mock(FindIterable.class);
        doReturn(iterator).when(findResult).iterator();
        doCallRealMethod().when(findResult).forEach(any(Consumer.class));
        doReturn(findResult).when(this.collection).find(any(Document.class));

        var result = this.repository.find("field", "value");
        assertTrue(result.isPresent());
        assertThat(result.get().length(), is(1));
        assertThat(result.get().getJSONObject(0).getString("id"), is("603aed537004b70c47368fd9"));
        assertThat(result.get().getJSONObject(0).getString("domain"), is("domain"));
        assertThat(result.get().getJSONObject(0).getString("name"), is("name"));
    }

    @Test
    public void testFindKeys() {
        var iterator = mock(MongoCursor.class);
        doReturn(false).when(iterator).hasNext();
        var findResult = mock(FindIterable.class);
        doReturn(iterator).when(findResult).iterator();
        doReturn(findResult).when(this.collection).find(any(Document.class));

        var result = this.repository.find(List.of("603aed537004b70c47368fd9"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAll() {
        var iterator = mock(MongoCursor.class);
        doReturn(false).when(iterator).hasNext();
        var findResult = mock(FindIterable.class);
        doReturn(iterator).when(findResult).iterator();
        doReturn(findResult).when(this.collection).find();

        var result = this.repository.all();
        assertTrue(result.isEmpty());
    }
}
