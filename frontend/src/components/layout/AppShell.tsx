import { Outlet, Link, useLocation } from "react-router-dom";
import { Activity, Inbox, MessageSquare } from "lucide-react";
import { cn } from "../../lib/utils";

export function AppShell() {
    const location = useLocation();

    const navigation = [
        { name: "Dashboard", href: "/dashboard", icon: Activity },
        { name: "Tickets", href: "/tickets", icon: Inbox },
        { name: "Internal Thread", href: "/internal-thread", icon: MessageSquare, disabled: true },
    ];

    return (
        <div className="flex h-screen w-full bg-slate-50 dark:bg-slate-950">
            {/* Sidebar */}
            <div className="w-64 border-r bg-white dark:bg-slate-900 shadow-sm flex flex-col">
                <div className="h-16 flex items-center px-6 border-b font-bold text-lg tracking-tight">
                    Signal Broker
                </div>
                <nav className="flex-1 overflow-y-auto py-4">
                    <ul className="grid gap-1 px-4">
                        {navigation.map((item) => (
                            <li key={item.name}>
                                {item.disabled ? (
                                    <div className="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium text-slate-400 cursor-not-allowed">
                                        <item.icon className="h-4 w-4" />
                                        {item.name}
                                        <span className="ml-auto text-[10px] uppercase tracking-wider font-semibold bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded">Soon</span>
                                    </div>
                                ) : (
                                    <Link
                                        to={item.href}
                                        className={cn(
                                            "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors hover:bg-slate-100 hover:text-slate-900 dark:hover:bg-slate-800 dark:hover:text-slate-50",
                                            location.pathname === item.href
                                                ? "bg-slate-100 text-slate-900 dark:bg-slate-800 dark:text-slate-50"
                                                : "text-slate-600 dark:text-slate-300"
                                        )}
                                    >
                                        <item.icon className="h-4 w-4" />
                                        {item.name}
                                    </Link>
                                )}
                            </li>
                        ))}
                    </ul>
                </nav>
            </div>

            {/* Main content */}
            <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
                <div className="flex-1 overflow-y-auto">
                    <div className="max-w-7xl mx-auto px-6 py-6">
                        <Outlet />
                    </div>
                </div>
            </main>
        </div>
    );
}
