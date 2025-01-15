import SQLite, {SQLiteDatabase} from 'react-native-sqlite-storage';
import {IMessageDetail} from 'src/types';

const db: SQLiteDatabase = SQLite.openDatabase(
  {
    name: 'messages.db',
    location: 'default',
  },
  () => console.log('Database opened'),
  error => console.error('Error opening database', error),
);

export const createTable = (): void => {
  db.transaction(tx => {
    tx.executeSql(
      `CREATE TABLE IF NOT EXISTS messages (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        text TEXT,
        image TEXT,
        video TEXT,
        createdAt INTEGER NOT NULL,
        userId TEXT NOT NULL,
        userName TEXT NOT NULL,
        userPublicKey TEXT NOT NULL
      )`,
      [],
      () => console.log('Table created successfully'),
      error => console.error('Error creating table', error),
    );
  });
};

export const insertMessage = (message: IMessageDetail): void => {
  db.transaction(tx => {
    tx.executeSql(
      `INSERT INTO messages (text, image, video, createdAt, userId, userName, userPublicKey) VALUES (?, ?, ?, ?, ?, ?,?)`,
      [
        message.text || null,
        message.image || null,
        message.video || null,
        message.createdAt,
        message.user.id,
        message.user.name,
        message.user.publicKey,
      ],
      () => console.log('Message inserted'),
      error => console.error('Error inserting message', error),
    );
  });
};

export const getMessages = (callback: (messages: any[]) => void): void => {
  db.transaction(tx => {
    tx.executeSql(
      `SELECT * FROM messages ORDER BY createdAt DESC`,
      [],
      (_, results) => {
        const rows = results.rows;
        const messages = [];
        for (let i = 0; i < rows.length; i++) {
          const row = rows.item(i);
          messages.push({
            text: row.text,
            image: row.image,
            video: row.video,
            createdAt: row.createdAt,
            user: {
              id: row.userId,
              name: row.userName,
              publicKey: row.publicKey,
            },
          });
        }
        callback(messages);
      },
      error => console.error('Error fetching messages', error),
    );
  });
};
