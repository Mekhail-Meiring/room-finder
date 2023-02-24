import React, { useState, useEffect } from "react";
import Calendar from "react-calendar";
import "react-calendar/dist/Calendar.css";
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import "./styles/BookedRoomsPage.scss"

function BookedRooms() {
    const [selectedDate, setSelectedDate] = useState(new Date());
    const [bookedRooms, setBookedRooms] = useState([]);
    const [isConnected, setIsConnected] = useState(true)

    const client = new SockJS('http://localhost:8080/ws');
    const websocket = Stomp.over(client);

    websocket.connect({}, function (frame) {

        if (isConnected){
            websocket.send("/app/get-bookings", {});
            setIsConnected(false)
        }
        websocket.subscribe('/topic/bookings', function (message) {
            setBookedRooms(JSON.parse(message.body));
        });
    });







    // const tileContent = ({ date, view }) => {
    //
    //     const eventList = bookedRooms
    //         .filter((room) => room.date === date.toDateString())
    //         .map((room) => <li key={room.booking_id}>{room.date}</li>);
    //     return <ul>{eventList}</ul>;
    // };

    return (
        <div className="booked-rooms">
            {/*<Calendar value={selectedDate} onChange={setSelectedDate} tileContent={tileContent}/>*/}
            <div>
                Booked Rooms:
                {bookedRooms.map((room) => (
                    <div style={
                        {
                            "marginTop" : "10px",
                            "marginBottom" : "10px"
                        }

                    }>
                        <button >- {room.date}</button>
                    </div>
                ))}
            </div>

            <button className="btn" style={
                {
                    "marginTop" : "20px",
                    "marginBottom" : "10px"
                }
            } type="button" onClick={
                () => {
                    const url = '/home';
                    window.history.pushState(null, '', url);
                    window.location.href = url;
                }
            }>Home</button>

            <button className="btn" type="button" onClick={
                    () => {
                        const url = '/book-room';
                        window.history.pushState(null, '', url);
                        window.location.href = url;
                    }
            }>Book Room</button>
        </div>
    );
}

export default BookedRooms;