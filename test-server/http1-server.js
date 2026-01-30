const https = require('https');
const fs = require('fs');
const path = require('path');

const certPath = path.join(__dirname, 'cert.pem');
const keyPath = path.join(__dirname, 'key.pem');

if (!fs.existsSync(certPath) || !fs.existsSync(keyPath)) {
    console.error('Error: cert.pem or key.pem not found.');
    process.exit(1);
}

const options = {
    cert: fs.readFileSync(certPath),
    key: fs.readFileSync(keyPath)
};

const server = https.createServer(options, (req, res) => {
    // In HTTP/1.1, we use req.headers.host
    const hostHeader = req.headers.host;

    // SNI is retrieved from the socket
    const sniHostname = req.socket.servername || "N/A";

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
      <h1>HTTP/1.1 Connection Audit</h1>
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
      <td class="label">Host Header</td>
      <td>${hostHeader}</td>
    </tr>
    <tr>
      <td class="label">Protocol</td>
      <td>HTTP/1.1</td>
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
    console.log(`\nSNI: ${sniHostname}\nHost header: ${hostHeader}\n`)

    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
    res.end(htmlResponse);
});

server.listen(8443, '0.0.0.0', () => {
    console.log('HTTP/1.1 Monochrome Server listening on https://0.0.0.0:8443');
});