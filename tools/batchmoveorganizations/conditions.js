module.exports = [
  org => org.status && org.status === 'PASSIIVINEN',
  org => Array.isArray(org.tyypit) && org.tyypit.includes('Oppilaitos'),
]
