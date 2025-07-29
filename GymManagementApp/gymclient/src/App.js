import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import axios from 'axios';
import { auth } from './utils/auth'; // Import the auth object

// Login Component
const LoginPage = ({ onLogin }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/login', 
        `username=${username}&password=${password}`,
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        }
      );
      auth.setToken(response.data.access_token);
      onLogin();
    } catch (err) {
      setError('Login failed. Please check credentials.');
      console.error('Login error:', err);
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '400px', margin: '0 auto' }}>
      <h1 style={{ textAlign: 'center' }}>Gym Management Login</h1>
      {error && <div style={{ color: 'red', margin: '10px 0' }}>{error}</div>}
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <div>
          <label>Username:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            style={{ width: '100%', padding: '8px' }}
            required
          />
        </div>
        <div>
          <label>Password:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ width: '100%', padding: '8px' }}
            required
          />
        </div>
        <button 
          type="submit" 
          style={{ 
            padding: '10px', 
            backgroundColor: '#1976d2', 
            color: 'white', 
            border: 'none', 
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Login
        </button>
      </form>
    </div>
  );
};



// Dashboard Component
const DashboardPage = () => {
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get('http://localhost:8080/members/me', {
          headers: auth.getAuthHeader()
        });
        setUserData(response.data);
      } catch (err) {
        console.error('Failed to fetch user data:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleLogout = () => {
    auth.removeToken();
    window.location.href = '/login';
  };

  if (loading) return <div style={{ padding: '20px' }}>Loading user data...</div>;

  return (
    <div style={{ padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Dashboard</h1>
        <button 
          onClick={handleLogout}
          style={{
            padding: '8px 16px',
            backgroundColor: '#dc3545',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Logout
        </button>
      </div>
      
      {userData && (
        <div style={{ marginTop: '20px', padding: '15px', border: '1px solid #ddd', borderRadius: '4px' }}>
          <h2>Welcome, {userData.name || userData.username}</h2>
          <h3>Your Details:</h3>
          <pre style={{ 
            backgroundColor: '#f5f5f5', 
            padding: '15px', 
            borderRadius: '4px',
            overflowX: 'auto'
          }}>
            {JSON.stringify(userData, null, 2)}
          </pre>
        </div>
      )}
    </div>
  );
};

// Main App Component
const App = () => {
  const [isAuth, setIsAuth] = useState(auth.isAuthenticated());

  return (
    <BrowserRouter>
      <Routes>
        <Route 
          path="/login" 
          element={
            isAuth ? <Navigate to="/" /> : <LoginPage onLogin={() => setIsAuth(true)} />
          } 
        />
        <Route 
          path="/" 
          element={
            isAuth ? <DashboardPage /> : <Navigate to="/login" />
          } 
        />
      </Routes>
    </BrowserRouter>
  );
};

export default App;