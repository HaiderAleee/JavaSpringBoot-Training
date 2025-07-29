import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';
import { removeToken } from '../utils/auth';

function DashboardPage() {
  const [userData, setUserData] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        // Example: Fetch member profile if user is a member
        const response = await api.get('/members/me');
        setUserData(response.data);
      } catch (err) {
        console.error('Failed to fetch profile', err);
      }
    };
    fetchProfile();
  }, []);

  const handleLogout = () => {
    removeToken();
    navigate('/login');
  };

  return (
    <div>
      <h1>Dashboard</h1>
      <button onClick={handleLogout}>Logout</button>
      
      {userData && (
        <div>
          <h2>Welcome, {userData.name}</h2>
          {/* Display user-specific content here */}
        </div>
      )}
    </div>
  );
}

export default DashboardPage;