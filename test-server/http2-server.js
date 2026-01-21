const http2 = require('http2');
const fs = require('fs');
const path = require('path');

const certPath = path.join(__dirname, 'cert.pem');
const keyPath = path.join(__dirname, 'key.pem');

if (!fs.existsSync(certPath) || !fs.existsSync(keyPath)) {
    console.error('Error: cert.pem or key.pem not found.');
    process.exit(1);
}

const server = http2.createSecureServer({
    cert: fs.readFileSync(certPath),
    key: fs.readFileSync(keyPath)
});

server.on('stream', (stream, headers) => {
    const hostHeader = headers[':authority'];
    const sniHostname = stream.session.socket.servername || "N/A";

    const htmlResponse = `
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <title>HTTP/2 Test Server</title>
      <style>
        body { 
          background-color: #000000; 
          color: #ffffff; 
          font-family: 'Courier New', Courier, monospace; 
          padding: 40px; 
        }
        h1 { 
          color: #aaaaaa; 
          padding-bottom: 10px;
          display: inline-block;
        }
        table { 
          border-collapse: collapse; 
          width: 100%; 
          max-width: 800px; 
          background-color: #111111;
          margin-top: 20px;
        }
        th, td { 
          border: 1px solid #444; 
          padding: 12px 15px; 
          text-align: left; 
        }
        th { background-color: #222; color: #aaaaaa; }
        tr:hover { background-color: #1a1a1a; }
        .label { color: #aaaaaa; font-weight: bold; }
      </style>
    </head>
    <body>
      <h1>HTTP/2 Connection Audit</h1>
      <pre>
<table>
  <thead>
    <tr>
      <th>Property</th>
      <th>Value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td class="label">SNI Hostname</td>
      <td>${sniHostname}</td>
    </tr>
    <tr>
      <td class="label">Host Header (:authority)</td>
      <td>${hostHeader}</td>
    </tr>
    <tr>
      <td class="label">Protocol</td>
      <td>HTTP/2 (h2)</td>
    </tr>
    <tr>
      <td class="label">Encryption</td>
      <td>TLS (Active)</td>
    </tr>
  </tbody>
</table>
      </pre>
    </body>
    </html>
  `;

    stream.respond({
        'content-type': 'text/html; charset=utf-8',
        ':status': 200
    });

    stream.end(htmlResponse);
});

server.listen(8443, '0.0.0.0', () => {
    console.log('HTTP/2 Monochrome Server listening on https://0.0.0.0:8443');
});