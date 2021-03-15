thisdir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
set -o pipefail

# Shared content to use as template
header='{
    "alg": "RS256",
    "typ": "JWT"
}'
payload_template='{}'

build_payload() {
        jq -c \
                --arg iat_str "$(date +%s)" \
                --arg app_id "${GITHUB_APP_ID}" \
        '
        ($iat_str | tonumber) as $iat
        | .iat = $iat
        | .exp = ($iat + 300)
        | .iss = ($app_id | tonumber)
        ' <<< "${payload_template}" | tr -d '\n'
}

b64enc() { openssl enc -base64 -A | tr '+/' '-_' | tr -d '='; }
json() { jq -c . | LC_CTYPE=C tr -d '\n'; }
rs256_sign() { openssl dgst -binary -sha256 -sign <(printf '%s\n' "$1"); }

sign() {
    local algo payload sig
    algo=${1:-RS256}; algo=${algo^^}
    payload=$(build_payload) || return
    signed_content="$(json <<<"$header" | b64enc).$(json <<<"$payload" | b64enc)"
    sig=$(printf %s "$signed_content" | rs256_sign "$GITHUB_APP_KEY" | b64enc)
    printf '%s.%s\n' "${signed_content}" "${sig}"
}

JWT=$(sign)

echo "::add-mask::$JWT"

id=$(curl -s \
    -H "Authorization: Bearer $JWT" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/app/installations" | jq '[.[]|.id][0]')

token=$(curl -s \
    -X POST \
    -H "Authorization: Bearer $JWT" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/app/installations/$id/access_tokens" | jq -r '.token')

echo "::add-mask::$token"

repositories=$(curl -s \
    -H "Authorization: token $token" \
    -H "Accept: application/vnd.github.v3+json" \
    https://api.github.com/installation/repositories | jq -r '.repositories | .[].full_name')

repositories="${repositories//'%'/'%25'}"
repositories="${repositories//$'\n'/'%0A'}"
repositories="${repositories//$'\r'/'%0D'}"

echo "::set-output name=repositories::$repositories"