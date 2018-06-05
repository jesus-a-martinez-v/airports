import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import models.{Country, QueryResult, Report}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}
import services.SummarizerCacheKeeper
import services.SummarizerCacheKeeper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SummarizerCacheKeeperSuite extends TestKit(ActorSystem("KeeperSpec"))
  with FunSuiteLike with BeforeAndAfterAll with ImplicitSender {

  private def addReport(actor: ActorRef, report: Report): Unit = {
    actor ! SaveReport(report)
  }

  private def clean(actor: ActorRef): Unit = {
    actor ! Clean
  }

  private def addQueryResult(actor: ActorRef, countryCode: String, queryResult: QueryResult): Unit = {
    actor ! SaveQueryForCountry(countryCode, queryResult)
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  test("Should save a report") {
    val actor = system.actorOf(SummarizerCacheKeeper.props(10.seconds))
    val mockReport = Report(Iterable.empty, Iterable.empty, Iterable.empty, Iterable.empty)
    addReport(actor, mockReport)

    actor ! GetReport
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.ReportOperationResult]).result.get === mockReport)
  }

  test("Should save a query result") {
    val actor = system.actorOf(SummarizerCacheKeeper.props(10.seconds))
    val mockQueryResult = QueryResult(Country(0, "mock", "mock", "mock", "mock"), Seq.empty)
    addQueryResult(actor, "mock", mockQueryResult)

    actor ! GetQueryForCountry("mock")
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.QueryOperationResult]).result.get === mockQueryResult)
  }

  test("Should return none when there's no saved report") {
    val actor = system.actorOf(SummarizerCacheKeeper.props(10.seconds))

    actor ! GetReport
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.ReportOperationResult]).result.isEmpty)
  }

  test("Should return none when there's no saved query result for a given country") {
    val actor = system.actorOf(SummarizerCacheKeeper.props(10.seconds))

    actor ! GetQueryForCountry("imaginary country")
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.QueryOperationResult]).result.isEmpty)
  }

  test("Should wipe everything out when received Clean message") {
    val actor = system.actorOf(SummarizerCacheKeeper.props(10.seconds))
    val mockReport = Report(Iterable.empty, Iterable.empty, Iterable.empty, Iterable.empty)
    addReport(actor, mockReport)

    actor ! GetReport
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.ReportOperationResult]).result.get === mockReport)

    val mockQueryResult = QueryResult(Country(0, "mock", "mock", "mock", "mock"), Seq.empty)
    addQueryResult(actor, "mock", mockQueryResult)

    actor ! GetQueryForCountry("mock")
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.QueryOperationResult]).result.get === mockQueryResult)

    clean(actor)
    actor ! GetReport
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.ReportOperationResult]).result.isEmpty)
    actor ! GetQueryForCountry("mock")
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.QueryOperationResult]).result.isEmpty)
  }

  test("Should clean cache after 'frequency' (in this case 5 seconds) units of time have passed.") {
    val actor = system.actorOf(SummarizerCacheKeeper.props(5.seconds))
    val mockReport = Report(Iterable.empty, Iterable.empty, Iterable.empty, Iterable.empty)
    addReport(actor, mockReport)

    actor ! GetReport
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.ReportOperationResult]).result.get === mockReport)

    val mockQueryResult = QueryResult(Country(0, "mock", "mock", "mock", "mock"), Seq.empty)
    addQueryResult(actor, "mock", mockQueryResult)

    actor ! GetQueryForCountry("mock")
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.QueryOperationResult]).result.get === mockQueryResult)

    Thread.sleep(6000)

    actor ! GetReport
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.ReportOperationResult]).result.isEmpty)
    actor ! GetQueryForCountry("mock")
    assert(expectMsgClass(classOf[SummarizerCacheKeeper.QueryOperationResult]).result.isEmpty)
  }

}
