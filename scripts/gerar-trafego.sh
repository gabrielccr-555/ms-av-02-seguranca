#!/usr/bin/env bash
# Gera logins validos, invalidos e bloqueios para popular o dashboard do Grafana.
# Uso: ./scripts/gerar-trafego.sh [url_base]   (padrao: http://localhost:8080)
URL=${1:-http://localhost:8080}
H="Content-Type: application/json"

login() {
  code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$URL/login" -H "$H" \
    -d "{\"username\":\"$1\",\"senha\":\"$2\"}")
  echo "POST /login $1/$2 -> $code"
}

echo "== Cadastrando usuarios (ROLE_MANUTENCAO) =="
for u in ana bruno carla; do
  curl -s -o /dev/null -w "POST / $u -> %{http_code}\n" -u manutencao:manutencao \
    -X POST "$URL/" -H "$H" -d "{\"username\":\"$u\",\"senha\":\"senha123\"}"
done

echo "== Logins validos =="
for i in 1 2 3; do login joao 123456; login ana senha123; done

echo "== Logins invalidos (bloqueiam maria e bruno) =="
login maria errada; login maria errada
login bruno errada; login bruno errada
login inexistente qualquer

echo "== Tentativa com usuario bloqueado (401) =="
login maria abc123

echo "== Desbloqueando bruno (ROLE_ADMIN) =="
curl -s -o /dev/null -w "PUT /unlock/bruno -> %{http_code}\n" -u admin:admin -X PUT "$URL/unlock/bruno"
login bruno senha123

echo "== Usuarios cadastrados =="
curl -s -u manutencao:manutencao "$URL/"; echo
