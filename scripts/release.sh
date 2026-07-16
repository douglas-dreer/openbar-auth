#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# release.sh - Script de release para OPENBAR
#
# Uso:
#   ./scripts/release.sh <version>           # Cria branch release/v<version>
#   ./scripts/release.sh <version> --tag     # Cria tag e push (após merge em main)
#
# Fluxo completo:
#   1. ./scripts/release.sh 0.2.0
#   2. git push origin release/v0.2.0
#   3. Criar PR → develop (CI atualiza arquivos automaticamente)
#   4. Merge develop → PR → main
#   5. ./scripts/release.sh 0.2.0 --tag
# ==============================================================================

VERSION="${1:-}"
CREATE_TAG="${2:-}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log()   { echo -e "${GREEN}[INFO]${NC} $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }
step()  { echo -e "${BLUE}[STEP]${NC} $1"; }

# ==============================================================================
# Validações
# ==============================================================================

if [ -z "$VERSION" ]; then
  error "Uso: $0 <version> [--tag]\nExemplo: $0 0.2.0"
fi

if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  error "Version inválida: $VERSION (use formato X.Y.Z)"
fi

if [ -n "$(git status --porcelain)" ]; then
  error "Working tree sujo. Faça commit ou stash primeiro."
fi

# ==============================================================================
# Criar branch release
# ==============================================================================

if [ "$CREATE_TAG" != "--tag" ]; then
  BRANCH_NAME="release/v$VERSION"
  CURRENT_BRANCH=$(git branch --show-current)

  if [ "$CURRENT_BRANCH" != "develop" ]; then
    warn "Você está na branch '$CURRENT_BRANCH', não em 'develop'."
    read -rp "Continuar mesmo assim? (y/N) " CONFIRM
    if [ "$CONFIRM" != "y" ]; then
      error "Abortado."
    fi
  fi

  log "=== Criando branch $BRANCH_NAME ==="
  echo ""

  # Create branch from develop
  git checkout -b "$BRANCH_NAME" develop 2>/dev/null || {
    warn "Branch $BRANCH_NAME já existe."
    git checkout "$BRANCH_NAME"
  }

  git push origin "$BRANCH_NAME" 2>/dev/null || true

  echo ""
  log "Branch '$BRANCH_NAME' criada e pushada."
  echo ""
  echo "Próximos passos:"
  echo "  1. Criar PR → develop no GitHub"
  echo "     URL: https://github.com/douglas-dreer/openbar-auth/compare/$BRANCH_NAME?expand=1"
  echo ""
  echo "  2. O workflow 'Release Prepare' vai atualizar automaticamente:"
  echo "     - build.gradle.kts (version)"
  echo "     - CHANGELOG.md ([Unreleased] → [$VERSION])"
  echo "     - README.md (referências de versão)"
  echo ""
  echo "  3. Após merge develop → criar PR develop → main"
  echo ""
  echo "  4. Após merge em main, criar tag:"
  echo "     $0 $VERSION --tag"
fi

# ==============================================================================
# Criar tag (após merge em main)
# ==============================================================================

if [ "$CREATE_TAG" = "--tag" ]; then
  CURRENT_BRANCH=$(git branch --show-current)

  if [ "$CURRENT_BRANCH" != "main" ]; then
    warn "Você está na branch '$CURRENT_BRANCH', não em 'main'."
    read -rp "Criar tag mesmo assim? (y/N) " CONFIRM
    if [ "$CONFIRM" != "y" ]; then
      error "Abortado."
    fi
  fi

  log "=== Criando tag v$VERSION ==="

  git tag -a "v$VERSION" -m "Release v$VERSION"
  git push origin "v$VERSION"

  log "Tag v$VERSION criada e pushada."
  log "Release workflow será triggered automaticamente."
  echo ""
  echo "GitHub Release: https://github.com/douglas-dreer/openbar-auth/releases/tag/v$VERSION"
fi

log "=== Concluído ==="
