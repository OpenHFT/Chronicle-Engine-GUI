package simplefix.examples.executor;

import simplefix.*;

import java.util.List;

public class Executor {

    private static EngineFactory _engineFact;

    public static void main(final String args[]) throws Exception {
        try {

            Class<?> classobj = Class.forName("simplefix.quickfix.EngineFactory");
            Object engineobj = classobj.newInstance();

            if (engineobj instanceof EngineFactory) {

                _engineFact = (EngineFactory) engineobj;
                Engine engine = _engineFact.createEngine();
                engine.initEngine("META-INF/resources/executor.cfg");

                Application application = new _Application();

                engine.startInProcess(application);

                System.out.println("press <enter> to quit");
                System.in.read();

                engine.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class _Application implements Application {

        public _Application() {
        }

        public void onLogon(final Session sessionId) {

        }

        public void onLogout(final Session sessionId) {

        }

        public void onAppMessage(final Message message, final Session sessionId) {
            if (MsgType.ORDER_SINGLE.equals(message.getMsgType())) {
                Message replyMsg = _engineFact.createMessage(MsgType.EXECUTION_REPORT);

                replyMsg.setValue(Tag.OrderID, genOrderID());
                replyMsg.setValue(Tag.ExecID, genExecID());

                replyMsg.setValue(Tag.ExecTransType, "0");
                replyMsg.setValue(Tag.ExecType, "2");
                replyMsg.setValue(Tag.OrdStatus, "2");

                replyMsg.setValue(Tag.ClOrdID, message.getValue(Tag.ClOrdID));
                replyMsg.setValue(Tag.Symbol, message.getValue(Tag.Symbol));
                replyMsg.setValue(Tag.Side, message.getValue(Tag.Side));
                replyMsg.setValue(Tag.OrderQty, message.getValue(Tag.OrderQty));
                replyMsg.setValue(Tag.Price, message.getValue(Tag.Price));

                replyMsg.setValue(Tag.LeavesQty, "0");
                replyMsg.setValue(Tag.CumQty, message.getValue(Tag.OrderQty));
                replyMsg.setValue(Tag.AvgPx, message.getValue(Tag.Price));
                replyMsg.setValue(Tag.LastPx, message.getValue(Tag.Price));
                replyMsg.setValue(Tag.LastQty, message.getValue(Tag.OrderQty));

                sessionId.sendAppMessage(replyMsg);

            } else if (MsgType.ORDER_LIST.equals(message.getMsgType())) {

                List<Group> orderList = message.getGroupValue(Tag.NoOrders);

                for (Group order : orderList) {
                    Message replyMsg = _engineFact.createMessage(MsgType.EXECUTION_REPORT);

                    replyMsg.setValue(Tag.OrderID, genOrderID());
                    replyMsg.setValue(Tag.ExecID, genExecID());

                    replyMsg.setValue(Tag.ExecTransType, "0");
                    replyMsg.setValue(Tag.ExecType, "2");
                    replyMsg.setValue(Tag.OrdStatus, "2");

                    replyMsg.setValue(Tag.ClOrdID, order.getValue(Tag.ClOrdID));
                    replyMsg.setValue(Tag.Symbol, order.getValue(Tag.Symbol));
                    replyMsg.setValue(Tag.Side, order.getValue(Tag.Side));
                    replyMsg.setValue(Tag.OrderQty, order.getValue(Tag.OrderQty));
                    replyMsg.setValue(Tag.Price, order.getValue(Tag.Price));

                    replyMsg.setValue(Tag.LeavesQty, "0");
                    replyMsg.setValue(Tag.CumQty, order.getValue(Tag.OrderQty));
                    replyMsg.setValue(Tag.AvgPx, order.getValue(Tag.Price));
                    replyMsg.setValue(Tag.LastPx, order.getValue(Tag.Price));
                    replyMsg.setValue(Tag.LastQty, order.getValue(Tag.OrderQty));

                    sessionId.sendAppMessage(replyMsg);
                }
            }
        }

        public int genOrderID() {
            return ++m_orderID;
        }

        public int genExecID() {
            return ++m_execID;
        }

        private int m_orderID = 0;
        private int m_execID = 0;
    }

}
