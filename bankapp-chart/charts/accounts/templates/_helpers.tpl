{{- define "accounts.name" -}}
{{- .Chart.Name -}}
{{- end -}}

{{- define "accounts.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "accounts.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "accounts.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/name: {{ include "accounts.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "accounts.postgresHost" -}}
{{- printf "%s-postgres" .Release.Name -}}
{{- end -}}

{{- define "accounts.keycloakHost" -}}
{{- printf "%s-keycloak" .Release.Name -}}
{{- end -}}
