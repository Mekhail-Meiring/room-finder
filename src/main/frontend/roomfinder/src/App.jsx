import { BrowserRouter as Router, Switch, Route } from 'react-router-dom'
import BookRoom from './pages/BookRoom'
import BookedRooms from './pages/BookedRooms'
import CancelBooking from './pages/CancelBooking'
import Home from './pages/Home'
import RescheduleBooking from './pages/RescheduleBooking'
import Welcome from './pages/Welcome'


function App() {

  return (
    <div className="App">

      <Router>
        <Switch>
            <Route exact path="/">
              {/* Renders the Welcome component */}
              <Welcome/>
            </Route>
          <Route path="/home">
            {/* Renders the Home component */}
            <Home/>
          </Route>
          <Route path="/booked-rooms">
            {/* Renders the BookedRooms component */}
            <BookedRooms/>
          </Route>
          <Route path="/book-room">
            {/* Renders the BookRoom component */}
            <BookRoom/>
          </Route>
          <Route path="/cancel-booking">
                {/* Renders the CancelBooking component */}
                <CancelBooking/>
          </Route>
          <Route path="/reschedule-booking">
                {/* Renders the RescheduleBooking component */}
                <RescheduleBooking/>
          </Route>
        </Switch>
      </Router>
    </div>
  )
}

export default App
