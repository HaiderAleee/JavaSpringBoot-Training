import { useState, useEffect } from 'react';
import api from '../api/api';

function MemberList() {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMembers = async () => {
      try {
        const response = await api.get('/members');
        setMembers(response.data);
        setLoading(false);
      } catch (err) {
        console.error('Failed to fetch members', err);
        setLoading(false);
      }
    };
    fetchMembers();
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h2>Members</h2>
      <ul>
        {members.map((member) => (
          <li key={member.id}>
            {member.name} - {member.email}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default MemberList;