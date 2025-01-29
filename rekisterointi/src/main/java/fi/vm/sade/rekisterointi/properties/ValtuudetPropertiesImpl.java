package fi.vm.sade.rekisterointi.properties;

import fi.vm.sade.suomifi.valtuudet.ValtuudetProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rekisterointi.valtuudet")
public class ValtuudetPropertiesImpl implements ValtuudetProperties {

  private String host;
  private String clientId;
  private String apiKey;
  private String oauthPassword;

  @Override
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  @Override
  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public String getOauthPassword() {
    return oauthPassword;
  }

  public void setOauthPassword(String oauthPassword) {
    this.oauthPassword = oauthPassword;
  }

}
