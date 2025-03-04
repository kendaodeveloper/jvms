import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 10, // Número de usuários virtuais simultâneos
  duration: '30s', // Duração do teste
};

export default function () {
  const url = 'http://localhost:8080/jvm/test';

  const res = http.get(url);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });
}

// to install (No macOS com Homebrew):
// brew install k6

// to execute:
// k6 run jvm-test.js
