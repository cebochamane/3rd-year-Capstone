import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingWorker;
import org.junit.jupiter.api.Test;

class DoubleDummyViewStateTest {
    private static final class View extends GameView {
        String message;
        @Override public void showMessage(String message) { this.message=message; }
    }
    @Test void doesNotExposeAnalysisBeforeAPlaySessionExists() {
        View view=new View();GameController controller=new GameController();view.setController(controller);
        controller.startLoadedDeal(DoubleDummyTableTest.referenceDeal());
        view.doubleDummyClicked();
        assertTrue(view.message.contains("playing phase starts"));
    }
    @Test void calculatesOncePerDistributionAndDropsLateResultsAfterSwitchingHands() throws Exception {
        CountDownLatch started=new CountDownLatch(1),release=new CountDownLatch(1),finished=new CountDownLatch(1);
        AtomicInteger calls=new AtomicInteger();
        GameController controller=new GameController() {
            @Override public DoubleDummyTable getOrCalculateDoubleDummyTable(Deal deal) {
                if(calls.incrementAndGet()==1) {
                    started.countDown();
                    // Deliberately emulate a dependency that completes after cancellation.
                    boolean released=false;
                    while(!released) try { released=release.await(5,TimeUnit.SECONDS); }
                    catch(InterruptedException ignored) { }
                    finished.countDown();
                }
                return new DoubleDummyTable(deal,new int[5][4]);
            }
        };
        View view=new View();view.setController(controller);
        Deal first=DoubleDummyTableTest.referenceDeal(),second=TutorialTestSupport.early("Claim",null).getDeal();
        try {
            TutorialTestSupport.onEdt(()->{controller.startLoadedDeal(first);TutorialTestSupport.invoke(view,"ensureDoubleDummyAnalysis");return null;});
            assertTrue(started.await(5,TimeUnit.SECONDS));
            TutorialTestSupport.onEdt(()->{
                TutorialTestSupport.invoke(view,"ensureDoubleDummyAnalysis"); // Same hand, no duplicate job.
                controller.startLoadedDeal(second);
                TutorialTestSupport.invoke(view,"ensureDoubleDummyAnalysis");return null;
            });
            awaitTable(view,DoubleDummyTable.handId(second));
            release.countDown();assertTrue(finished.await(5,TimeUnit.SECONDS));
            TutorialTestSupport.onEdt(()->{
                assertEquals(DoubleDummyTable.handId(second),((DoubleDummyTable)TutorialTestSupport.get(view,"doubleDummyTable")).getHandId());
                TutorialTestSupport.invoke(view,"ensureDoubleDummyAnalysis");return null;
            });
            assertEquals(2,calls.get());
        } finally {
            release.countDown();
            TutorialTestSupport.onEdt(()->{TutorialTestSupport.invoke(view,"clearSessionViewState");return null;});
        }
    }
    @Test void resettingSessionCancelsPendingWorkAndClearsAnalysis() throws Exception {
        CountDownLatch started=new CountDownLatch(1),release=new CountDownLatch(1);
        GameController controller=new GameController() {
            @Override public DoubleDummyTable getOrCalculateDoubleDummyTable(Deal deal) {
                started.countDown();
                try { release.await(5,TimeUnit.SECONDS); } catch(InterruptedException ignored) { }
                return new DoubleDummyTable(deal,new int[5][4]);
            }
        };
        View view=new View();view.setController(controller);
        try {
            SwingWorker<?,?> worker=TutorialTestSupport.onEdt(()->{
                controller.startLoadedDeal(DoubleDummyTableTest.referenceDeal());
                TutorialTestSupport.invoke(view,"ensureDoubleDummyAnalysis");
                return (SwingWorker<?,?>)TutorialTestSupport.get(view,"doubleDummyWorker");
            });
            assertTrue(started.await(5,TimeUnit.SECONDS));
            TutorialTestSupport.onEdt(()->{
                TutorialTestSupport.invoke(view,"clearSessionViewState");
                assertNull(TutorialTestSupport.get(view,"doubleDummyTable"));
                assertNull(TutorialTestSupport.get(view,"doubleDummyHandId"));
                assertNull(TutorialTestSupport.get(view,"doubleDummyWorker"));return null;
            });
            assertTrue(worker.isCancelled());
        } finally { release.countDown(); }
    }
    private void awaitTable(View view,String key) throws Exception {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
        while(System.nanoTime()<deadline) {
            boolean ready=TutorialTestSupport.onEdt(()->{
                DoubleDummyTable table=(DoubleDummyTable)TutorialTestSupport.get(view,"doubleDummyTable");
                return table!=null && table.getHandId().equals(key);
            });
            if(ready)return;
            Thread.sleep(20);
        }
        fail("Background table did not reach the view");
    }
}
