
/** Llama a la API. Si el servidor responde 401 va al login; si responde 403 va a la vista de restriccion. */
async function api(url, opciones = {}) {
  const resp = await fetch(url, {
    cache: 'no-store',
    headers: { 'Content-Type': 'application/json' },
    ...opciones
  });
  if (resp.status === 401) { location.href = 'index.html'; throw { error: 'No autenticado' }; }
  if (resp.status === 403) { location.href = 'restringido.html'; throw { error: 'Sin permisos' }; }
  const datos = await resp.json().catch(() => ({}));
  if (!resp.ok) throw datos;
  return datos;
}

/** Evita que un texto con < o > se interprete como HTML. */
function esc(t) {
  return String(t ?? '').replace(/[&<>"']/g,
    c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function mensajeError(e) {
  if (e && e.detalles) return Object.values(e.detalles).join(' | ');
  return (e && (e.error || e.message)) || 'Ocurrio un error';
}

function mostrar(texto, tipo = 'success') {
  const a = document.getElementById('alerta');
  a.innerHTML = `<div class="alert alert-${tipo}">${esc(texto)}</div>`;
  setTimeout(() => { a.innerHTML = ''; }, 4500);
}

function hora(t) { return String(t ?? '').slice(0, 5); }

async function cerrarSesion() {
  await fetch('/api/auth/logout', { method: 'POST' });
  location.href = 'index.html';
}

async function mostrarUsuario() {
  const u = await api('/api/auth/me');
  document.getElementById('quien').textContent = `${u.nombre} (${u.rol})`;
}

window.addEventListener('pageshow', e => { if (e.persisted) location.reload(); });
