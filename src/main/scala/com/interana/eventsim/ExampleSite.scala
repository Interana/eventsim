package com.interana.eventsim

object ExampleSite {
  val guestPage = new State("Guest Page")
  val registerPage = new State("Register Page")
  val homePage = new State("Home Page")
  val searchPage = new State("Search Page")
  val searchResults = new State("Search Results")
  val fourOhFour = new State("404")
  val settingPage = new State("Settings")
  val loginPage = new State("Login")
  val logoutPage = new State("Logout")
  val newsPage = new State("news")

  guestPage.addTransition(fourOhFour, 0.001)
  registerPage.addTransition(fourOhFour, 0.001)
  homePage.addTransition(fourOhFour, 0.001)
  searchPage.addTransition(fourOhFour, 0.001)
  searchResults.addTransition(fourOhFour, 0.001)
  settingPage.addTransition(fourOhFour, 0.001)
  loginPage.addTransition(fourOhFour, 0.001)
  logoutPage.addTransition(fourOhFour, 0.001)
  newsPage.addTransition(fourOhFour, 0.001)

  guestPage.addTransition(loginPage, 0.75)
  guestPage.addTransition(registerPage, 0.2)

  registerPage.addTransition(homePage, 0.9)

  homePage.addTransition(searchPage, 0.3)
  homePage.addTransition(settingPage, 0.05)
  homePage.addTransition(logoutPage, 0.01)
  homePage.addTransition(newsPage, 0.3)

  searchPage.addTransition(searchResults, 0.7)
  searchPage.addTransition(homePage, 0.2)
  searchPage.addTransition(newsPage, 0.05)

  searchResults.addTransition(searchPage, 0.5)
  searchResults.addTransition(homePage, 0.2)
  searchResults.addTransition(newsPage, 0.2)

  loginPage.addTransition(homePage, 0.35)
  loginPage.addTransition(searchPage, 0.3)
  loginPage.addTransition(newsPage, 0.2)
  loginPage.addTransition(settingPage, 0.1)

  logoutPage.addTransition(loginPage, 0.5)
  logoutPage.addTransition(guestPage, 0.45)

  settingPage.addTransition(homePage, 0.95)

  newsPage.addTransition(homePage, 0.5)
  newsPage.addTransition(searchPage, 0.3)

  fourOhFour.addTransition(loginPage, 0.09)
  fourOhFour.addTransition(homePage, 0.3)
  fourOhFour.addTransition(newsPage, 0.3)
  fourOhFour.addTransition(searchPage, 0.3)

}