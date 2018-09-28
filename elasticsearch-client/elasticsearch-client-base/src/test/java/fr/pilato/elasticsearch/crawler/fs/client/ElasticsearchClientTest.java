/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.elasticsearch.crawler.fs.client;

import fr.pilato.elasticsearch.crawler.fs.client.dummy.ElasticsearchClientDummyGoodVersion;
import fr.pilato.elasticsearch.crawler.fs.client.dummy.ElasticsearchClientDummyWrongVersion;
import fr.pilato.elasticsearch.crawler.fs.settings.Elasticsearch;
import fr.pilato.elasticsearch.crawler.fs.settings.FsSettings;
import fr.pilato.elasticsearch.crawler.fs.test.framework.AbstractFSCrawlerTestCase;
import org.junit.Test;

import java.io.IOException;

import static fr.pilato.elasticsearch.crawler.fs.client.ElasticsearchClientUtil.decodeCloudId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class ElasticsearchClientTest extends AbstractFSCrawlerTestCase {

    @Test
    public void testCloudId() {

        String cloudId = "fscrawler:ZXVyb3BlLXdlc3QxLmdjcC5jbG91ZC5lcy5pbyQxZDFlYTk5Njg4Nzc0NWE2YTJiN2NiNzkzMTUzNDhhMyQyOTk1MDI3MzZmZGQ0OTI5OTE5M2UzNjdlOTk3ZmU3Nw==";
        Elasticsearch.Node httpHost = decodeCloudId(cloudId);

        assertThat(httpHost.getHost(), is("1d1ea996887745a6a2b7cb79315348a3.europe-west1.gcp.cloud.es.io"));
        assertThat(httpHost.getPort(), is(443));
        assertThat(httpHost.getScheme(), is(Elasticsearch.Node.Scheme.HTTPS));
    }

    @Test
    public void testGetInstanceWithNullSettings() {
        NullPointerException npe = expectThrows(NullPointerException.class,
                () -> ElasticsearchClientUtil.getInstance(null, null));
        assertThat(npe.getMessage(), is("settings can not be null"));
    }

    @Test
    public void testGetInstance() throws IOException {
        ElasticsearchClient instance = ElasticsearchClientUtil.getInstance(null, FsSettings.builder("foo").build());
        assertThat(instance, instanceOf(ElasticsearchClientDummyGoodVersion.class));
        instance.checkVersion();
    }

    @Test
    public void testGetInstanceWrongVersions() {
        ElasticsearchClient instance = ElasticsearchClientUtil.getInstance(null, FsSettings.builder("foo").build(),
                "fr/pilato/elasticsearch/crawler/fs/client/dummy/fscrawler-client-wrong-version.properties");
        assertThat(instance, instanceOf(ElasticsearchClientDummyWrongVersion.class));
        RuntimeException exception = expectThrows(RuntimeException.class, () -> {
            try {
                instance.checkVersion();
                return null;
            } catch (IOException e) {
                return e;
            }
        });
        assertThat(exception.getMessage(), is("The Elasticsearch client version [5] is not compatible with the Elasticsearch cluster version [6.4.1]."));
    }
}
