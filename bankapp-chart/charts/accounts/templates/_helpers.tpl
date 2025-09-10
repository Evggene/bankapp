{{- define "accounts.name" -}}
{{- .Chart.Name -}}
{{- end -}}

{{- define "accounts.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "accounts.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}
