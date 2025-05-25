apiVersion: v1
kind: ConfigMap
metadata:
  name: ${configMapName!"app-config"}
  labels:
    generated-by: "strict-configmap-generator"
    timestamp: "${.now?string("yyyy-MM-dd'T'HH:mm:ssXXX")}"
data:
<#list properties as key, value>
  ${key}: <#if value??>${value?json_string}<#else>"null"</#if>
</#list>