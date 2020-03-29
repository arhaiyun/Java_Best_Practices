package com.exodus.arhaiyun.Birdies.database;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.osmapps.golf.common.util.Clock;
import com.osmapps.golf.common.util.CollectionUtil;
import com.osmapps.golf.model.util.ServerMetaDao;

@Service
public class MongoDataSource {

  public static final String NO_TIMEOUT = ".noTimeout";

  @Value("${mongodb.replicaSets}")
  private String replicaSets;
  @Value("${mongodb.forceAllInOne}")
  private boolean forceAllInOne;

  private Map<String, MongoClient> mongoClients;

  @Autowired
  private ServerMetaDao serverMetaDao;

  public MongoClient getMongoClient(String replicaSetName) {
    return getMongoClient(replicaSetName, false);
  }

  public MongoClient getMongoClient(String replicaSetName, boolean noTimeout) {
    if (noTimeout) {
      return mongoClients.get(replicaSetName + NO_TIMEOUT);
    } else {
      return mongoClients.get(replicaSetName);
    }
  }

  @VisibleForTesting
  public Map<String, MongoClient> getMongoClients() {
    return mongoClients;
  }

  // for safety
  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  void close() {
    if (!CollectionUtil.isNullOrEmpty(mongoClients)) {
      Collection<MongoClient> allMongoClients = mongoClients.values();
      for (MongoClient mongoClient : allMongoClients) {
        mongoClient.close();
      }
    }
    mongoClients = null;
  }

  @PostConstruct
  public void init() throws IOException {
    System.out.println("mongodb.replicaSets=" + replicaSets);
    System.out.println("mongodb.forceAllInOne=" + forceAllInOne);

    mongoClients = Maps.newConcurrentMap();
    final String[] replicaSetNames = replicaSets.split(";");
    if (forceAllInOne) {
      Properties properties =
          PropertiesLoaderUtils.loadAllProperties("mongoDataSource.allInOne.properties");
      final String servers;
      if (serverMetaDao.isTestCase()) {
        servers = getMongoServersForTestCase();
      } else {
        servers = properties.getProperty("mongodb.servers");
      }
      final List<ServerAddress> serverAddresses = parse(servers);
      MongoClient mongoClient = createMongoClient(serverAddresses, properties);
      MongoClient noTimeoutMongoClient = createNoTimeoutMongoClient(serverAddresses, properties);
      for (String replicaSetName : replicaSetNames) {
        mongoClients.put(replicaSetName, mongoClient);
        mongoClients.put(replicaSetName + NO_TIMEOUT, noTimeoutMongoClient);
      }
    } else {
      for (String replicaSetName : replicaSetNames) {
        Properties properties = PropertiesLoaderUtils
            .loadAllProperties("mongoDataSource." + replicaSetName + ".properties");
        final String servers = properties.getProperty("mongodb.servers");
        final List<ServerAddress> serverAddresses = parse(servers);
        MongoClient mongoClient = createMongoClient(serverAddresses, properties);
        MongoClient noTimeoutMongoClient = createNoTimeoutMongoClient(serverAddresses, properties);
        mongoClients.put(replicaSetName, mongoClient);
        mongoClients.put(replicaSetName + NO_TIMEOUT, noTimeoutMongoClient);
      }
    }
  }

  private String getMongoServersForTestCase() {
    String mongoPort = System.getProperty("mongoPort");
    if (!Strings.isNullOrEmpty(mongoPort)) {
      Preconditions.checkState(mongoPort.matches("[0-9]+"));
      return "127.0.0.1:" + mongoPort;
    }
    return "127.0.0.1:27017";
  }

  private MongoClient createMongoClient(List<ServerAddress> serverAddresses, Properties properties)
      throws UnknownHostException {
    final String username = properties.getProperty("mongodb.username");
    final String password = properties.getProperty("mongodb.password");
    int connectionsPerHost =
        Integer.parseInt(properties.getProperty("mongodb.connectionsPerHost"));
    final int webServerDividerForConnectionsPerHost =
        Integer.parseInt(properties.getProperty("mongodb.connectionsPerHost.webServerDivider"));
    if (serverMetaDao.isWebServer()) {
      connectionsPerHost = connectionsPerHost / webServerDividerForConnectionsPerHost;
    }
    final int minConnectionsPerHost = connectionsPerHost * 2 / 3;
    final int connectTimeout = Integer.parseInt(properties.getProperty("mongodb.connectTimeout"));
    final int maxWaitTime = Integer.parseInt(properties.getProperty("mongodb.maxWaitTime"));
    final boolean socketKeepAlive =
        Boolean.parseBoolean(properties.getProperty("mongodb.socketKeepAlive"));
    final int socketTimeout = Integer.parseInt(properties.getProperty("mongodb.socketTimeout"));

    final ReadPreference readPreference = serverMetaDao.isTestCase() ?
        ReadPreference.primary() : ReadPreference.secondaryPreferred();
    MongoClientOptions options = new MongoClientOptions.Builder()
        .connectionsPerHost(connectionsPerHost).minConnectionsPerHost(minConnectionsPerHost)
        .connectTimeout(connectTimeout)
        .maxWaitTime(maxWaitTime).socketKeepAlive(socketKeepAlive).socketTimeout(socketTimeout)
        .readPreference(readPreference).build();
    MongoClient mongoClient;
    if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
      MongoCredential credential = MongoCredential.createScramSha1Credential(username, "admin", password.toCharArray());
      mongoClient = new MongoClient(serverAddresses, Lists.newArrayList(credential), options);
    } else {
      mongoClient = new MongoClient(serverAddresses, options);
    }

    return mongoClient;
  }

  private MongoClient createNoTimeoutMongoClient(List<ServerAddress> serverAddresses, Properties properties)
      throws UnknownHostException {
    final String username = properties.getProperty("mongodb.username");
    final String password = properties.getProperty("mongodb.password");
    MongoClientOptions noTimeoutOptions = new MongoClientOptions.Builder().connectionsPerHost(1)
        .connectTimeout(5000).maxWaitTime((int) Clock.ONE_MINUTE_MS).socketKeepAlive(true)
        .socketTimeout((int) (15 * Clock.ONE_MINUTE_MS))
        .readPreference(ReadPreference.primaryPreferred()).build();
    MongoClient noTimeoutMongoClient;
    if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
      MongoCredential credential = MongoCredential.createScramSha1Credential(username, "admin", password.toCharArray());
      noTimeoutMongoClient = new MongoClient(serverAddresses, Lists.newArrayList(credential), noTimeoutOptions);
    } else {
      noTimeoutMongoClient = new MongoClient(serverAddresses, noTimeoutOptions);
    }
    return noTimeoutMongoClient;
  }

  private List<ServerAddress> parse(String servers) throws UnknownHostException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(servers));
    String[] serverArray = servers.split(";");
    Preconditions.checkArgument(serverArray.length > 0);
    List<ServerAddress> serverAddresses = Lists.newArrayListWithCapacity(serverArray.length);
    for (String server : serverArray) {
      if (Strings.isNullOrEmpty(server)) {
        continue;
      }
      String[] hostAndPort = server.split(":");
      Preconditions.checkState(hostAndPort.length == 2);
      String host = hostAndPort[0];
      int port = Integer.parseInt(hostAndPort[1]);
      ServerAddress serverAddress = new ServerAddress(host, port);
      serverAddresses.add(serverAddress);
    }
    return serverAddresses;
  }

  public boolean isForceAllInOne() {
    return forceAllInOne;
  }
}
