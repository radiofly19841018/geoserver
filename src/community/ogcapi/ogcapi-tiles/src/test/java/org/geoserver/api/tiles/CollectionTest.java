/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.api.tiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import org.geoserver.data.test.MockData;
import org.hamcrest.Matchers;
import org.junit.Test;

public class CollectionTest extends TilesTestSupport {

    @Test
    public void testRoadsCollectionJson() throws Exception {
        String roadSegments = getLayerId(MockData.ROAD_SEGMENTS);
        DocumentContext json = getAsJSONPath("ogc/tiles/collections/" + roadSegments, 200);

        testRoadsCollectionJson(json);
    }

    @Test
    public void testRoadsCollectionHTML() throws Exception {
        String roadSegments = getLayerId(MockData.ROAD_SEGMENTS);
        getAsJSoup("ogc/tiles/collections/" + roadSegments + "?f=text/html");
        // TODO: add ids in the elemnets and check contents using jSoup
    }

    @Test
    public void testOnlyMapLinks() throws Exception {
        // this one only has rendered formats assocaited
        String lakesId = getLayerId(MockData.LAKES);
        DocumentContext json = getAsJSONPath("ogc/tiles/collections/" + lakesId, 200);

        assertEquals(
                "http://localhost:8080/geoserver/ogc/tiles/collections/cite:Lakes/map/tiles?f=application%2Fjson",
                getSingle(json, "$.links[?(@.rel=='tiles' && @.type=='application/json')].href"));
    }

    @Test
    public void testOnlyDataLinks() throws Exception {
        // this one only has rendered formats assocaited
        String forestsId = getLayerId(MockData.FORESTS);

        DocumentContext json = getAsJSONPath("ogc/tiles/collections/" + forestsId, 200);

        assertEquals(
                "http://localhost:8080/geoserver/ogc/tiles/collections/cite:Forests/tiles?f=application%2Fjson",
                getSingle(json, "$.links[?(@.rel=='tiles' && @.type=='application/json')].href"));
    }

    public void testRoadsCollectionJson(DocumentContext json) {
        assertEquals("cite:RoadSegments", json.read("$.id", String.class));
        assertEquals("RoadSegments", json.read("$.title", String.class));
        assertEquals(-0.0042, json.read("$.extent.spatial[0]", Double.class), 0d);
        assertEquals(-0.0024, json.read("$.extent.spatial[1]", Double.class), 0d);
        assertEquals(0.0042, json.read("$.extent.spatial[2]", Double.class), 0d);
        assertEquals(0.0024, json.read("$.extent.spatial[3]", Double.class), 0d);

        // check the tiles link (both data and map tiles)
        List<String> items =
                json.read("$.links[?(@.rel=='tiles' && @.type=='application/json')].href");
        assertEquals(2, items.size());
        assertThat(
                items,
                Matchers.containsInAnyOrder(
                        "http://localhost:8080/geoserver/ogc/tiles/collections/cite:RoadSegments/tiles?f=application%2Fjson",
                        "http://localhost:8080/geoserver/ogc/tiles/collections/cite:RoadSegments/map/tiles?f=application%2Fjson"));

        // styles
        assertEquals(Integer.valueOf(2), json.read("$.styles.size()"));
        assertEquals("RoadSegments", json.read("$.styles[0].id"));
        assertEquals("Default Styler", json.read("$.styles[0].title"));
        assertEquals(
                "http://localhost:8080/geoserver/ogc/styles/styles/RoadSegments?f=application%2Fvnd.ogc.sld%2Bxml",
                getSingle(
                        json,
                        "$.styles[0].links[?(@.rel=='stylesheet' && @.type=='application/vnd.ogc.sld+xml')].href"));
        assertEquals(
                "http://localhost:8080/geoserver/ogc/styles/styles/RoadSegments/metadata?f=application%2Fjson",
                getSingle(
                        json,
                        "$.styles[0].links[?(@.rel=='describedBy' && @.type=='application/json')].href"));

        assertEquals("generic", json.read("$.styles[1].id"));
        assertEquals("Generic", json.read("$.styles[1].title"));
    }

    @Test
    public void testRoadsCollectionYaml() throws Exception {
        String yaml =
                getAsString(
                        "ogc/tiles/collections/"
                                + getLayerId(MockData.ROAD_SEGMENTS)
                                + "?f=application/x-yaml");
        DocumentContext json = convertYamlToJsonPath(yaml);
        testRoadsCollectionJson(json);
    }
}
