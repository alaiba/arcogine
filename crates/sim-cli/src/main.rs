use clap::Parser;

/// Arcogine — deterministic factory & economy simulation engine.
#[derive(Parser)]
#[command(name = "arcogine", version, about)]
enum Cli {
    /// Start the HTTP API server.
    Serve,
    /// Run a scenario headlessly and exit.
    Run {
        /// Run without the HTTP server.
        #[arg(long)]
        headless: bool,
    },
}

fn main() {
    let cli = Cli::parse();
    match cli {
        Cli::Serve => {
            println!("Server mode not yet implemented (Phase 4).");
        }
        Cli::Run { headless: _ } => {
            println!("Headless run not yet implemented (Phase 4).");
        }
    }
}
