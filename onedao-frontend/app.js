const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');
const fetch = require('node-fetch');

const app = express();
app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static('public'));
app.set('view engine', 'ejs');

app.get('/', (req, res) => res.redirect('/register'));

app.get('/register', (req, res) => {
  res.render('register');
});

app.post('/register', async (req, res) => {
  const { email, password, confirmPassword } = req.body;

  if (password !== confirmPassword) {
    return res.send("Passwords do not match");
  }

  try {
    await fetch('http://localhost:8080/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    res.redirect('/otp');
  } catch (err) {
    res.send("Error registering: " + err.message);
  }
});

app.get('/otp', (req, res) => {
  res.render('otp');
});

app.post('/otp', async (req, res) => {
  const { otp } = req.body;
  try {
    const response = await fetch('http://localhost:8080/api/auth/verify-otp', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ otp })
    });

    const data = await response.json();
    if (data.success) {
      res.send("Email verified successfully!");
    } else {
      res.send("Invalid OTP");
    }
  } catch (err) {
    res.send("Error verifying OTP: " + err.message);
  }
});

app.listen(3000, () => console.log('Frontend running on http://localhost:3000'));
