import React, {useState} from "react";
import "./styles/Bookroom.scss";

function BookRoom() {

    const clientID = sessionStorage.getItem('client_id');

    const [isOnPayment, setIsOnPayment] = useState(false);
    const [price, setPrice] = useState(0);
    const [date, setDate] = useState("");

    const bookRoom = async (event) => {
        event.preventDefault();

        const data = new FormData(event.target);
        const date = data.get('date');
        const client_id = data.get('client_id');

        const response = await fetch("http://localhost:8080/api/book-room", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "date": date,
                "client_id": client_id
            })
        });

        if (response.status === 400) {
            alert("Room already booked for requested date");
        }

        if (response.status === 200) {
            const json = await response.json();
            setPrice(json.room_price);
            setDate(date.toLocaleString());
            setIsOnPayment(true);
        }
    }

    const payForBooking = async (event) => {

        event.preventDefault()

        const data = new FormData(event.target);
        const client_id = data.get('client_id');
        const date = data.get('date');
        const price = data.get('price');

        const response = await fetch("http://localhost:8080/api/booking-payment", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "client_id": client_id,
                "date": date,
                "price": price
            })
        });

        if (response.status === 400) {
            alert("Room already booked for requested date");
        }

        if (response.status === 201) {
            alert("Payment successful");
            const url = '/booked-rooms';
            window.history.pushState(null, '', url);
            window.location.href = url;
        }
    }

    return (
        <div className="book-room">
            <div className="base-container">
                {isOnPayment ?
                    <div>
                        <div className="header">Booking payment</div>
                        <div className="content">
                            <p style={{"marginTop": "20px",}}>Confirm date: {date}</p>
                            <form className="form" onSubmit={payForBooking}>
                                <div className="form-group">
                                    Price for booking: ${price}
                                </div>
                                <input type="hidden" name="client_id" value={clientID}/>
                                <input type="hidden" name="date" value={date}/>
                                <input type="hidden" name="price" value={price}/>
                                <div className="footer">
                                    <button type="submit" className="btn">
                                        Pay
                                    </button>
                                    <button type="button" className="btn" onClick={() => {setIsOnPayment(false);}}>
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                    :
                    <div>
                        <div className="header">Book room</div>
                        <div className="content">
                            <p style={{"marginTop": "20px",}}>Choose a date to book a room</p>
                            <form className="form" onSubmit={bookRoom}>
                                <div className="form-group">
                                    <input type="date" name="date"/>
                                </div>
                                <input type="hidden" name="client_id" value={clientID}/>
                                <div className="footer">
                                    <button type="submit" className="btn">
                                        Book Room
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                }

            </div>
        </div>
    );
}

export default BookRoom;